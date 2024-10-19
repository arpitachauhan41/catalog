import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecretSharing {
    public static void main(String[] args) {
        try {
            Map<Integer, BigInteger> points1 = readJSONFile("testcase1.json");
            BigInteger secret1 = findSecret(points1);
            System.out.println("Secret for first testcase: " + secret1);

            Map<Integer, BigInteger> points2 = readJSONFile("testcase2.json");
            BigInteger secret2 = findSecret(points2);
            System.out.println("Secret for second testcase: " + secret2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. Read the Test Case (Input) from a separate JSON file
    private static Map<Integer, BigInteger> readJSONFile(String filename) throws IOException {
        Map<Integer, BigInteger> points = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        int n = 0, k = 0;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Skip empty lines or lines that don't contain any data
            if (line.isEmpty() || !line.contains(":")) {
                continue;
            }

            if (line.contains("\"n\":")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    n = Integer.parseInt(parts[1].trim().replace(",", ""));
                }
            } else if (line.contains("\"k\":")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    k = Integer.parseInt(parts[1].trim().replace(",", ""));
                }
            } else if (line.matches("\"\\d+\": \\{")) {
                // Key of the root (x-coordinate)
                int key = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                reader.readLine(); // Skip "base" line
                String baseLine = reader.readLine().trim();
                String[] baseParts = baseLine.split(":");
                if (baseParts.length > 1) {
                    BigInteger base = new BigInteger(baseParts[1].trim().replace(",", ""));
                    String valueLine = reader.readLine().trim();
                    String[] valueParts = valueLine.split(":");
                    if (valueParts.length > 1) {
                        BigInteger value = new BigInteger(valueParts[1].trim().replace(",", "").replace("}", ""));
                        // Add the decoded point (x, y)
                        points.put(key, decodeBase(value, base)); // Decode the base
                    }
                }
            }
        }
        reader.close();
        return points;
    }

    // This method decodes the value based on the base provided
    private static BigInteger decodeBase(BigInteger value, BigInteger base) {
        BigInteger result = BigInteger.ZERO;
        BigInteger power = BigInteger.ONE;

        while (value.compareTo(BigInteger.ZERO) > 0) {
            BigInteger digit = value.mod(base);
            result = result.add(digit.multiply(power));
            value = value.divide(base);
            power = power.multiply(BigInteger.valueOf(10)); // Increase the power
        }

        return result; // Return the decoded value
    }

    // 3. Find the Secret (C)
    private static BigInteger findSecret(Map<Integer, BigInteger> points) {
        BigInteger secret = BigInteger.ZERO;
        for (Map.Entry<Integer, BigInteger> entry1 : points.entrySet()) {
            int xi = entry1.getKey();
            BigInteger yi = entry1.getValue();

            // Calculate the Lagrange basis polynomial L_i(0)
            BigInteger li = BigInteger.ONE;
            for (Map.Entry<Integer, BigInteger> entry2 : points.entrySet()) {
                int xj = entry2.getKey();
                if (xi != xj) {
                    BigInteger numerator = BigInteger.valueOf(0 - xj);
                    BigInteger denominator = BigInteger.valueOf(xi - xj);
                    li = li.multiply(numerator).divide(denominator);
                }
            }

            // Add L_i(0) * y_i to the secret
            secret = secret.add(yi.multiply(li));
        }
        return secret;
    }
}
