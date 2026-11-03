package com.example.travelplatform;

import com.example.travelplatform.coupon.Coupon;
import com.example.travelplatform.coupon.CouponRepository;
import com.example.travelplatform.location.City;
import com.example.travelplatform.location.CityRepository;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import com.example.travelplatform.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Value("${app.admin-password:admin123}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      CityRepository cityRepository,
                                      CouponRepository couponRepository) {
        return args -> {
            // 初始化系统优惠券（98折、95折、9折）
            for (String name : List.of("98折券", "95折券", "9折券")) {
                couponRepository.findByName(name).orElseGet(() -> {
                    Coupon c = new Coupon();
                    c.setName(name);
                    double rate = name.startsWith("98") ? 0.98 : name.startsWith("95") ? 0.95 : 0.9;
                    c.setDiscountRate(BigDecimal.valueOf(rate));
                    return couponRepository.save(c);
                });
            }

            // 初始化管理员账号
            userRepository.findByUsername("admin").orElseGet(() -> {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.ADMIN);
                admin.setEnabled(true);
                return userRepository.save(admin);
            });

            // 初始化城市数据（如果表为空）
            if (cityRepository.count() == 0) {
                List<City> cities = List.of(
                        createCity("北京", "北京"),
                        createCity("上海", "上海"),
                        createCity("广州", "广东"),
                        createCity("深圳", "广东"),
                        createCity("杭州", "浙江")
                );
                cityRepository.saveAll(cities);
            }
        };
    }

    private City createCity(String name, String province) {
        City city = new City();
        city.setName(name);
        city.setProvince(province);
        city.setEnabled(true);
        return city;
    }
}

