package com.bash.authproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class AuthProjectApplicationTests {

	Calculator calc = new Calculator();
	@Test
	void itShouldAddNumbers() {
//		Given
		int numberOne = 20;
		int numberTwo = 30;

//		When
		int result = calc.add(numberOne, numberTwo);

//		Then
		assertThat(result).isEqualTo(50);
	}

	static class Calculator{
		int add(int a, int b){
			return a+b;
		}
	}

}
