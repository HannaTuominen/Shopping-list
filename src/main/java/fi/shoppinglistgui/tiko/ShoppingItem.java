package fi.shoppinglistgui.tiko;

/**
* ShoppingItem is used to create new shopping items to the shopping list
* 
* The shopping item is a class that is used to create new shopping items that have a key and a value.
* @author Hanna-Kaisa Tuominen
* @version 2019.1412
* 
*/
public class ShoppingItem {
    /**
     * The key of the shopping item.
     */
    private String key = "";
    /**
    * The value of the shopping item.
    */
    private int value;

    /**
     * Constructor that takes in a string key and an int value.
     * 
     * <p>Constructor is used to create new shopping items that must contain a string key (item) and
     * a int value (amount).
     * @param key the item value
     * @param value the amount value
     */
    public ShoppingItem(String key, int value) {
        setKey(key);
        setValue(value);
    }

    /**
     * Sets the wanted key (item)
     * 
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the wanted key (item)
     * 
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the wanted value (amount)
     * 
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Returns the wanted value (amount)
     * 
     * @return the value
     */
    public int getValue() {
        return value;
    }
    
}