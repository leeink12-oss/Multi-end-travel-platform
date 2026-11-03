package com.example.travelplatform.merchant;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import com.example.travelplatform.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@CrossOrigin
public class MerchantController {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MerchantController(MerchantRepository merchantRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Data
    public static class MerchantRegisterRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String name;
        private String contactPhone;
        /**
         * 商家类型：
         * - TRAVEL：旅游商家（酒店/景点）
         * - TRANSPORT：出行商家（火车/飞机）
         */
        private MerchantType type;
    }

    // 商家注册：创建用户 + 商家信息，状态为 PENDING，等待管理员审核
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody MerchantRegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ApiResponse.error(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.MERCHANT);
        // 商家账号需要管理员审核后才能启用
        user.setEnabled(false);
        userRepository.save(user);

        Merchant merchant = new Merchant();
        merchant.setUser(user);
        merchant.setName(request.getName());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setType(request.getType() != null ? request.getType() : MerchantType.TRAVEL);
        merchant.setStatus(MerchantStatus.PENDING);
        merchantRepository.save(merchant);

        return ApiResponse.success("商家注册成功，等待管理员审核", null);
    }

    // 管理员：查看所有商家
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ApiResponse<List<Merchant>> adminAll() {
        return ApiResponse.success(merchantRepository.findAll());
    }

    // 商家：查看自己的商家信息（包含 type，用于前端决定展示哪些管理模块）
    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/me")
    public ApiResponse<Merchant> me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.success(merchantRepository.findByUser(user).orElseThrow());
    }

    // 管理员：变更商家状态（审核通过/禁用）
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/status")
    public ApiResponse<Merchant> changeStatus(@PathVariable Long id,
                                              @RequestParam MerchantStatus status) {
        Merchant merchant = merchantRepository.findById(id).orElseThrow();
        merchant.setStatus(status);
        if (status == MerchantStatus.DISABLED) {
            User user = merchant.getUser();
            user.setEnabled(false);
            userRepository.save(user);
        } else if (status == MerchantStatus.APPROVED) {
            User user = merchant.getUser();
            user.setEnabled(true);
            userRepository.save(user);
        }
        return ApiResponse.success(merchantRepository.save(merchant));
    }

    // 管理员：删除商家（同时删除其登录账号）
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> deleteMerchant(@PathVariable Long id) {
        Merchant merchant = merchantRepository.findById(id).orElseThrow();
        User user = merchant.getUser();
        merchantRepository.delete(merchant);
        if (user != null) {
            userRepository.delete(user);
        }
        return ApiResponse.success(null);
    }
}

