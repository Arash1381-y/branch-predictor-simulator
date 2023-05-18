package devices;

import utils.Monitor;

/*
simple Cache interface

read the functions documentation if anything is unclear

 */
public interface Cache<K, V> extends Monitor {
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
     * map a value to the key if the key is not found in the cache
     *
     * @param key the address which is selected to put the data in it
     * @param value the data which is saved in address (key) if the key is not mapped to any not-null data
     */
    void putIfAbsent(K key, V value);

    /**
     * return the value associated with the key. If no value is associated to the key then set the default value
     * in it.
     *
     * @param key the address
     * @param defaultValue default value if the address is not associated with any block in cache
     * @return the value which is saved in the address. if the value is not set then write default value in cache
     * and return it
     */
    V setDefault(K key, V defaultValue);

    /**
     * clear the cache or simply remove all the entries
     */
    void clear();
}
