package banking;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LuhnAlgorithm {

    // returns new valid card number
    protected static String createNewCardNumber() {
        String cardNumber = String.format("400000%09d", ThreadLocalRandom.current().nextLong(999999999L));
        List<Integer> cardDigits = Arrays.stream(cardNumber.split(""))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        int checkSum = (10 - getSum(cardDigits) % 10) % 10;
        return cardNumber.concat(Integer.toString(checkSum));
    }

    protected static boolean isValidCardNumber(String cardNumber) {
        List<Integer> cardDigits = Arrays.stream(cardNumber.split(""))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        int checksum = cardDigits.get(cardDigits.size() - 1);
        cardDigits.remove(cardDigits.size() -1);

        return checksum == (10 - getSum(cardDigits) % 10) % 10;
    }

    private static int getSum(List<Integer> cardDigits) {
        int sum = 0;
        for (int i = 0; i < cardDigits.size(); i++) {
            if (i % 2 == 0) {
                cardDigits.set(i, cardDigits.get(i) * 2);
            }
            sum += cardDigits.get(i) > 9 ? cardDigits.get(i) - 9 : cardDigits.get(i);
        }
        return sum;
    }
}
