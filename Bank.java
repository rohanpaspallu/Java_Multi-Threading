import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

/**
 *
 */
public class Bank
{
    private static final URL path = ClassLoader.getSystemResource("bank.txt");
    private static final Logger logger = Logger.getLogger(Bank.class.getName());
    static HashMap<String, Integer> bankMap = new HashMap<>();
    private static final Bank instance = new Bank();

    /**
     *
     */
    private Bank()
    {
        readBankFile();
        //logger.info("Creating constructor for bank");
    }

    /**
     *
     * @return
     */
    public static Bank getInstance()
    {
        //logger.info("Returning bank instance");
        return instance;
    }

    /**
     *
     * @param name
     * @param amount
     */
    public void setBankMap(String name, int amount)
    {
        bankMap.put(name,amount);
    }

    /**
     *
     */
    public void showBankMap()
    {
        System.out.println("**** Banks  and financial resources ****");
        for (String key : bankMap.keySet())
        {
            System.out.println(key + " : " + bankMap.get(key));
        }
    }

    /**
     *
     */
    public void showBankRemainingMap()
    {
        if (bankMap.size() > 0)
        {
            for (String key : bankMap.keySet())
            {
                System.out.println(key + " has " + bankMap.get(key) + " dollar(s) left");
            }
        }
    }

    /**
     *
     * @return
     */
    public int banksAmountLeft()
    {
        return bankMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     *
     */
    private void readBankFile()
    {
        try {
            File file = new File(path.toURI());
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] parts = line.split(",", 2);
                if (parts.length == 2)
                {
                    String key = parts[0].replace("{","");
                    int value = Integer.parseInt(parts[1].replaceAll("[}.]", ""));
                    setBankMap(key,value);
                }
                else
                {
                    logger.info("ignoring line" + line);
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public static class BankProcess implements Runnable
    {
        private String name;

        BankProcess(String name)
        {
            this.name = name;
        }

        @Override
        public void run()
        {
            while (bankMap.containsKey(name))
            {
                Random random = new Random();
                if (Money.bankResponseQueue.size() < 1)
                {
                    int i = bankMap.get(name);
                    if (i > 0) {
                        Money.bankResponseQueue.add(name);
                    }
                }
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
