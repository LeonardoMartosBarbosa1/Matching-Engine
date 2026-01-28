import OrderBook.OrderBook;
import OrderBook.OrderBookLogger;

import java.util.Objects;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {

        Scanner scanner = new Scanner(System.in);
        OrderBook orderBook = new OrderBook( "ABEV" );
        while(true){
            System.out.print("> ");  // prompt
            String input = scanner.nextLine();

            if(Objects.equals(input, "break")) break;
            //We just add a Randon CLient
            orderBook.executeUserInput(input + " client");
        }
    }
}