package com.example.travelplatform.transport;

import com.example.travelplatform.location.City;
import com.example.travelplatform.location.CityRepository;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.merchant.MerchantRepository;
import com.example.travelplatform.merchant.MerchantStatus;
import com.example.travelplatform.merchant.MerchantType;
import com.example.travelplatform.product.ResourceStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 每日自动为出行商家生成 5-7 条航班和 5-7 条列车数据。
 * 生成后的数据可在商家端直接编辑。
 */
@Component
public class TransportScheduleGenerator {

    private final MerchantRepository merchantRepository;
    private final CityRepository cityRepository;
    private final FlightRepository flightRepository;
    private final TrainRepository trainRepository;
    private final Random random = new Random();

    public TransportScheduleGenerator(MerchantRepository merchantRepository,
                                      CityRepository cityRepository,
                                      FlightRepository flightRepository,
                                      TrainRepository trainRepository) {
        this.merchantRepository = merchantRepository;
        this.cityRepository = cityRepository;
        this.flightRepository = flightRepository;
        this.trainRepository = trainRepository;
    }

    /**
     * 每天 00:10 自动检查并补齐未来 30 天班次。
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void generateDailySchedules() {
        generateForNext30Days();
    }

    /**
     * 服务启动后先执行一次，避免必须等到凌晨才有数据。
     */
    @PostConstruct
    public void generateAtStartup() {
        generateForNext30Days();
    }

    public void generateForNext30Days() {
        List<City> cities = cityRepository.findByEnabledTrueOrderByNameAsc();
        if (cities.size() < 2) return;

        List<Merchant> merchants = merchantRepository.findByTypeAndStatus(MerchantType.TRANSPORT, MerchantStatus.APPROVED);
        if (merchants.isEmpty()) {
            merchants = merchantRepository.findByType(MerchantType.TRANSPORT);
        }
        for (Merchant merchant : merchants) {
            for (int dayOffset = 1; dayOffset <= 30; dayOffset++) {
                LocalDate targetDate = LocalDate.now().plusDays(dayOffset);
                LocalDateTime start = targetDate.atStartOfDay();
                LocalDateTime end = targetDate.plusDays(1).atStartOfDay();
                generateFlightsForMerchant(merchant, cities, start, end);
                generateTrainsForMerchant(merchant, cities, start, end);
            }
        }
    }

    private void generateFlightsForMerchant(Merchant merchant, List<City> cities, LocalDateTime start, LocalDateTime end) {
        int target = randomBetween(5, 7);
        long existing = flightRepository.countByMerchantAndDepartTimeBetween(merchant, start, end);
        int need = (int) Math.max(0, target - existing);
        for (int i = 0; i < need; i++) {
            City[] pair = pickDifferentCities(cities);
            LocalDateTime depart = randomTimeInRange(start);
            int durationMinutes = randomBetween(90, 240);
            LocalDateTime arrive = depart.plusMinutes(durationMinutes);

            BigDecimal economy = randPrice(300, 1200);
            BigDecimal business = economy.multiply(BigDecimal.valueOf(1.8)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal first = economy.multiply(BigDecimal.valueOf(2.5)).setScale(2, RoundingMode.HALF_UP);

            Flight f = new Flight();
            f.setMerchant(merchant);
            f.setFlightNo("MU" + (1000 + random.nextInt(9000)));
            f.setFromCity(pair[0].getName());
            f.setToCity(pair[1].getName());
            f.setDepartTime(depart);
            f.setArriveTime(arrive);
            f.setEconomyClassPrice(economy);
            f.setBusinessClassPrice(business);
            f.setFirstClassPrice(first);
            f.setPrice(economy);
            f.setStatus(ResourceStatus.ONLINE);
            f.setCoverImage("https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=1200&q=80");
            flightRepository.save(f);
        }
    }

    private void generateTrainsForMerchant(Merchant merchant, List<City> cities, LocalDateTime start, LocalDateTime end) {
        int target = randomBetween(5, 7);
        long existing = trainRepository.countByMerchantAndDepartTimeBetween(merchant, start, end);
        int need = (int) Math.max(0, target - existing);
        for (int i = 0; i < need; i++) {
            City[] pair = pickDifferentCities(cities);
            LocalDateTime depart = randomTimeInRange(start);
            int durationMinutes = randomBetween(120, 360);
            LocalDateTime arrive = depart.plusMinutes(durationMinutes);

            Train t = new Train();
            t.setMerchant(merchant);
            boolean emu = random.nextBoolean();
            if (emu) {
                t.setTrainType(TrainType.EMU);
                t.setTrainNo("G" + (100 + random.nextInt(900)));
                BigDecimal second = randPrice(80, 600);
                t.setEmuSecondSeatPrice(second);
                t.setEmuFirstSeatPrice(second.multiply(BigDecimal.valueOf(1.35)).setScale(2, RoundingMode.HALF_UP));
                t.setEmuFirstClassSeatPrice(second.multiply(BigDecimal.valueOf(1.65)).setScale(2, RoundingMode.HALF_UP));
                t.setEmuNoSeatPrice(second.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP));
                t.setPrice(t.getEmuNoSeatPrice());
            } else {
                t.setTrainType(TrainType.TRAIN);
                t.setTrainNo("K" + (100 + random.nextInt(900)));
                BigDecimal hardSeat = randPrice(40, 300);
                t.setTrainHardSeatPrice(hardSeat);
                t.setTrainHardSleeperPrice(hardSeat.multiply(BigDecimal.valueOf(1.6)).setScale(2, RoundingMode.HALF_UP));
                t.setTrainSoftSleeperPrice(hardSeat.multiply(BigDecimal.valueOf(2.1)).setScale(2, RoundingMode.HALF_UP));
                t.setPrice(hardSeat);
            }
            t.setFromCity(pair[0].getName());
            t.setToCity(pair[1].getName());
            t.setDepartTime(depart);
            t.setArriveTime(arrive);
            t.setStatus(ResourceStatus.ONLINE);
            t.setCoverImage("https://images.unsplash.com/photo-1474487548417-781cb71495f3?auto=format&fit=crop&w=1200&q=80");
            trainRepository.save(t);
        }
    }

    private City[] pickDifferentCities(List<City> cities) {
        if (cities.size() < 2) {
            throw new IllegalStateException("城市数量不足");
        }
        List<City> copy = new ArrayList<>(cities);
        City from = copy.get(random.nextInt(copy.size()));
        City to = from;
        while (to.getId().equals(from.getId())) {
            to = copy.get(random.nextInt(copy.size()));
        }
        return new City[]{from, to};
    }

    private LocalDateTime randomTimeInRange(LocalDateTime dayStart) {
        int hour = randomBetween(6, 21);
        int minute = random.nextBoolean() ? 0 : 30;
        return dayStart.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private BigDecimal randPrice(int min, int max) {
        double p = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(p).setScale(2, RoundingMode.HALF_UP);
    }
}

