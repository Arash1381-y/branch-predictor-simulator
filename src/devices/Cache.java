package devices;

/*
simple Cache interface

read the functions documentation if anything is unclear

 */
public interface Cache<K, V> {
    /**
     * @param key think of key as address
     * @return the value which is saved in that address or block
     */
    V get(K key);

    /**
     * @param key   the address which is selected to put the data in it
     * @param value the data which is saved in address (key)
     */
    void put(K key, V value);

    /**
     * clear the cache or simply remove all the entries
     */
    void clear();
}
