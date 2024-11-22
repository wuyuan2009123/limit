package com.hsdb.limit.init;

import com.hsdb.limit.pojo.User;
import com.hsdb.limit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInitCommand implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RedissonClient redissonClient;


    @Override
    public void run(String... args) throws Exception {
        User user1 = userRepository.save(new User("user1"));
        User user2 = userRepository.save(new User("user2"));
        User user3 = userRepository.save(new User("user3"));
        User user4 = userRepository.save(new User("user4"));
        RMap<Object, Object> userMap = redissonClient.getMap("user");
        userMap.put(user1.getName(),user1);
        userMap.put(user2.getName(),user2);
        userMap.put(user3.getName(),user3);
        userMap.put(user4.getName(),user4);
    }
}
