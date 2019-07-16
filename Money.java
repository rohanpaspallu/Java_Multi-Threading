import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 *
 */
public class Money
{
    private static final Logger logger =    Logger.getLogger(Money.class.getName());
    private static final Timer timer = new Timer();
    static final PriorityBlockingQueue<String> customerRequestQueue = new PriorityBlockingQueue<>();
    static final PriorityBlockingQueue<String> bankResponseQueue = new PriorityBlockingQueue<>();

    /**
     * Constructor for Money class
     */
    private Money()
    {
        logger.info("Creating money constructor");
    }

    /**
     * Enum to print the response
     */
    public enum ShowData {
        ASKED,
        GRANTED,
        REJECTED,
        OBJREACHED
    }

    /**
     * Main method to start program
     * @param args args
     */
    public static void main(String[] args)
    {
        Bank.getInstance().showBankMap();
        Customer.getInstance().showCustomerMap();
        ExecutorService executor = Executors.newCachedThreadPool();
        for (String key : Customer.customerMap.keySet())
        {
            Runnable customerWorker = new Customer.CustomerProcess(key);
            executor.execute(customerWorker);
        }

        for (String key : Bank.bankMap.keySet())
        {
            Runnable bankWorker = new Bank.BankProcess(key);
            executor.execute(bankWorker);
        }

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ProcessBankCustomer processBankCustomer = new ProcessBankCustomer();
        processBankCustomer.run();

    }

    /**
     * TImerDisplay class to print the messages
     */
    public static class TimerDisplay extends TimerTask
    {
        private String name;
        private String bankName;
        private int amount;
        private ShowData data;

        /**
         * Constructor for TimerDisplay class
         * @param name customer name
         * @param bankName bank name
         * @param data type of print message
         * @param amount amount
         */
        private TimerDisplay(String name, String bankName,ShowData data,int amount)
        {
            this.name = name;
            this.bankName = bankName;
            this.amount = amount;
            this.data = data;
        }
        @Override
        public void run() {
            switch (data)
            {
                case ASKED:
                    System.out.println(name + " request a loan of " + amount + " dollar(s) from " + bankName);
                    break;
                case GRANTED:
                    System.out.println(bankName + " approves loan of " + amount + " dollars(s) from " + name);
                    break;
                case REJECTED:
                    System.out.println(bankName + " denies loan of " + amount + " dollar(s) from " + name);
                    break;
                case OBJREACHED:
                    System.out.println(name + " reached objective of " + amount + " dollar(s) Woo Hoo !");
            }
        }
    }

    /**
     * Method to schedule print the message
     * @param name customer name
     * @param bankName bank name
     * @param data type of print message
     * @param amount amount
     * @param duration duration after print
     */
    private static void getOnDisplay(String name, String bankName, ShowData data,int amount, int duration)
    {
        TimerDisplay timerDisplay = new TimerDisplay(name, bankName, data, amount);
        timer.schedule(timerDisplay, duration);
    }

    /**
     * The main thread of program which will
     */
    public static class ProcessBankCustomer implements Runnable
    {
        boolean flag = true;
        @Override
        public void run() {
            while (flag) {
                if (Customer.customerMap.size() == 0 && Bank.bankMap.size() == 0) {
                    // program is initializing
                } else if (Customer.customerMap.size() == 0) {
                    Bank.getInstance().showBankRemainingMap();
                    System.exit(0);
                } else if (Bank.bankMap.size() == 0) {
                    Customer.getInstance().showRemainingCustomerMap();
                    System.exit(0);
                }
                else if (customerRequestQueue.size() > 0 && bankResponseQueue.size() > 0) {
                    if (customerRequestQueue.peek() != null && bankResponseQueue.peek() != null) {
                        String[] customerData = customerRequestQueue.peek().split(" ", 2);
                        String customerName = customerData[0];
                        int customerLoanAmount = Integer.parseInt(customerData[1]);
                        String bankName = bankResponseQueue.peek();
                        int bankAmount = Bank.bankMap.get(bankName);
                        getOnDisplay(customerName, bankName, ShowData.ASKED, customerLoanAmount, ThreadLocalRandom.current().nextInt(1, 100));
                        if (customerLoanAmount > bankAmount) {
                            getOnDisplay(customerName, bankName, ShowData.REJECTED, customerLoanAmount, ThreadLocalRandom.current().nextInt(1, 100));
                            if (Bank.getInstance().banksAmountLeft() < customerLoanAmount) {
                                Bank.bankMap.remove(bankName);
                            }
                        } else if (customerLoanAmount == bankAmount) {
                            Bank.bankMap.remove(bankName);
                        } else {
                            int customerBalance = Customer.customerMap.get(customerName);
                            int diff = customerBalance - customerLoanAmount;
                            if (diff == 0) {
                                Customer.customerMap.remove(customerName);
                                Bank.bankMap.put(bankName, bankAmount - customerLoanAmount);
                                getOnDisplay(customerName, bankName, ShowData.OBJREACHED, Customer.customerInitialMap.get(customerName), ThreadLocalRandom.current().nextInt(1, 100));
                            } else if (diff > 0) {
                                Customer.customerMap.put(customerName, customerBalance - customerLoanAmount);
                                Bank.bankMap.put(bankName, bankAmount - customerLoanAmount);
                                getOnDisplay(customerName, bankName, ShowData.GRANTED, customerLoanAmount, ThreadLocalRandom.current().nextInt(1, 100));
                            } else {
                                getOnDisplay(customerName, bankName, ShowData.REJECTED, customerLoanAmount, ThreadLocalRandom.current().nextInt(1, 100));
                            }
                        }
                        customerRequestQueue.remove();
                        bankResponseQueue.remove();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
