import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MathEquationValidator {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/math_equations";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введіть математичне рівняння:");
        String equation = scanner.nextLine();

        if (isValidEquation(equation)) {
            System.out.println("Рівняння є коректним.");
            saveEquationWithRoots(equation);
        } else {
            System.out.println("Рівняння містить помилки.");
        }
    }

    public static boolean isValidEquation(String equation) {
        equation = equation.replaceAll("\\s+", "");

        if (!isValidParentheses(equation)) {
            return false;
        }

        for (int i = 0; i < equation.length() - 1; i++) {
            char current = equation.charAt(i);
            char next = equation.charAt(i + 1);

            if (isOperator(current) && isOperator(next)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidParentheses(String equation) {
        List<Character> stack = new ArrayList<>();

        for (char c : equation.toCharArray()) {
            if (c == '(') {
                stack.add(c);
            } else if (c == ')') {
                if (stack.isEmpty() || stack.get(stack.size() - 1) != '(') {
                    return false;
                }
                stack.remove(stack.size() - 1);
            }
        }

        return stack.isEmpty();
    }

    public static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    public static void saveEquationWithRoots(String equation) {
        List<Double> roots = findRoots(equation);
        MathEquation mathEquation = new MathEquation(equation, roots);
        saveEquation(mathEquation);
    }

    public static List<Double> findRoots(String equation) {
        List<Double> roots = new ArrayList<>();


        String[] parts = equation.split("=");
        String leftPart = parts[0];
        String rightPart = parts[1];


        if (leftPart.contains("x^2")) {

            double a = getCoef(leftPart, "x^2");
            double b = getCoef(leftPart, "x");
            double c = Double.parseDouble(rightPart);


            double discriminant = b * b - 4 * a * c;

            if (discriminant >= 0) {

                double root1 = (-b + Math.sqrt(discriminant)) / (2 * a);
                double root2 = (-b - Math.sqrt(discriminant)) / (2 * a);

                roots.add(root1);
                roots.add(root2);
            }
        }

        return roots;
    }

    private static double getCoef(String equationPart, String term) {
        int indexOfTerm = equationPart.indexOf(term);
        int indexOfMultiplication = equationPart.lastIndexOf("*", indexOfTerm);

        if (indexOfMultiplication == -1) {
            return Double.parseDouble(equationPart.substring(0, indexOfTerm));
        }

        return Double.parseDouble(equationPart.substring(indexOfMultiplication + 1, indexOfTerm));
    }

    public static void saveEquation(MathEquation equation) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO equations (equation) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, equation.getEquation());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<MathEquation> searchEquationsByRoot(double root) {
        List<MathEquation> equations = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM equations";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String equationString = resultSet.getString("equation");
                List<Double> roots = findRoots(equationString);

                if (roots.contains(root)) {
                    equations.add(new MathEquation(equationString, roots));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return equations;
    }
}

class MathEquation {
    private String equation;
    private List<Double> roots;

    public MathEquation(String equation, List<Double> roots) {
        this.equation = equation;
        this.roots = roots;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public List<Double> getRoots() {
        return roots;
    }

    public void setRoots(List<Double> roots) {
        this.roots = roots;
    }
}