import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 *
 */
public class Customer
{
    private static final URL path = ClassLoader.getSystemResource("customer.txt");
    private static final Logger logger = Logger.getLogger(Bank.class.getName());
    static HashMap<String,Integer> customerMap = new HashMap<>();
    static final HashMap<String,Integer> customerInitialMap = new HashMap<>();
    private static final Customer instance = new Customer();

    /**
     *
     */
    private Customer()
    {
        readCustomerFile();
        //logger.info("Creating constructor for customer");
    }

    /**
     *
     * @return
     */
    public static Customer getInstance()
    {
        //logger.info("Returning customer instance");
        return instance;
    }

    /**
     *
     * @param name
     * @param amount
     */
    public void setCustomerMap(String name, Integer amount)
    {
        customerMap.put(name,amount);
    }

    /**
     *
     */
    public void showCustomerMap()
    {
        System.out.println("**** Customers and loan objectives ****");
        for (String key : customerMap.keySet())
        {
            System.out.println(key + " : " + customerMap.get(key));
        }
    }

    /**
     *
     */
    public void showRemainingCustomerMap()
    {
        if (customerMap.size() > 0)
        {
            for (String key : customerMap.keySet())
            {
                System.out.println(key + " was only able to borrow " + customerMap.get(key) + " dollars(s) Boo Hoo!");
            }
        }
    }

    /**
     *
     */
    private void readCustomerFile()
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
                    String key = parts[0].replace("{", "");
                    int value = Integer.valueOf(parts[1].replaceAll("[}.]",""));
                    setCustomerMap(key,value);
                    customerInitialMap.put(key,value);
                }
                else
                {
                    logger.info("ignoring line " + line);
                }
            }
            reader.close();
        } catch (IOException | URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public static class CustomerProcess implements Runnable
    {
        private String name;

        CustomerProcess(String name)
        {
            this.name = name;
        }
        @Override
        public void run() {
            while (customerMap.containsKey(name)) {
                Random random = new Random();
                if (Money.customerRequestQueue.size() < 1) {
                    int i = customerMap.get(name);
                    if (i > 1) {
                        if (i > 50)
                        {
                            Money.customerRequestQueue.add(name + " " + ThreadLocalRandom.current().nextInt(1, 50));
                        }
                        else
                        {
                            Money.customerRequestQueue.add(name + " " + ThreadLocalRandom.current().nextInt(1, 50));
                        }
                    } else if (customerMap.get(name) == 1) {
                        Money.customerRequestQueue.add(name + " " + 1);
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
