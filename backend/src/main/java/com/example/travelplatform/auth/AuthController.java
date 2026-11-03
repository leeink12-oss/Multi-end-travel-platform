package com.example.travelplatform.auth;

import com.example.travelplatform.auth.dto.LoginRequest;
import com.example.travelplatform.auth.dto.RegisterRequest;
import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.security.JwtTokenUtil;
import com.example.travelplatform.coupon.CouponRepository;
import com.example.travelplatform.coupon.UserCoupon;
import com.example.travelplatform.coupon.UserCouponRepository;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import com.example.travelplatform.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenUtil jwtTokenUtil,
                          CouponRepository couponRepository,
                          UserCouponRepository userCouponRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        // 注册前先检查用户名是否已存在，避免重复账号
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户名已存在"));
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole(UserRole.USER);
        // 新用户默认注册为普通用户，并保存到数据库
        User savedUser = userRepository.save(user);
        // 新用户注册赠券：
        // - 只给普通用户(UserRole.USER)发放系统内置的三张无门槛券
        // - 券模板在 DataInitializer 中初始化，这里只做“发券到用户账户”
        // - 通过 name 过滤，避免误把后续扩展的其它券自动赠送给新用户
        couponRepository.findAll().stream()
                .filter(c -> "98折券".equals(c.getName()) || "95折券".equals(c.getName()) || "9折券".equals(c.getName()))
                .forEach(coupon -> {
                    UserCoupon uc = new UserCoupon();
                    uc.setUser(savedUser);
                    uc.setCoupon(coupon);
                    uc.setUsed(false);
                    userCouponRepository.save(uc);
                });
        return ResponseEntity.ok(ApiResponse.success("注册成功", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 登录认证成功后签发 JWT，供前端后续请求携带身份信息
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
            String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole().name());

            return ResponseEntity.ok(ApiResponse.success("登录成功", token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户名或密码错误"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未登录"));
        }
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        record UserInfo(Long id, String username, String nickname, UserRole role, Boolean enabled) {}
        UserInfo info = new UserInfo(user.getId(), user.getUsername(), user.getNickname(), user.getRole(), user.getEnabled());
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}

