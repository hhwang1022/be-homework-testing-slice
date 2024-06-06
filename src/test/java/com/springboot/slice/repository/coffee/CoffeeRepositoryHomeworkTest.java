package com.springboot.slice.repository.coffee;

import com.springboot.coffee.repository.CoffeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class CoffeeRepositoryHomeworkTest {
    @Autowired
    private CoffeeRepository coffeeRepository;

    @Test
    public void findByCoffeeCodeTest() {
        // TODO CoffeeRepository의 findByCoffeeCode() 메서드에 대한 테스트 케이스를 여기에 작성해 주세요.
    }

    @Test
    public void findByCoffeeTest() {
        // TODO CoffeeRepository의 findByCoffeeCode() 메서드에 대한 테스트 케이스를 여기에 작성해 주세요.
    }
}
