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
     * helper function which calls get and put of the class to read and write, if needed, in the cache.
     *
     * @param key the address
     * @param defaultValue default value if the address is not associated with any block in cache
     * @return the value which is saved in the address. if the value is not set then write default value in cache
     * and return it
     */
    V getOrDefault(K key, V defaultValue);

    /**
     * clear the cache or simply remove all the entries
     */
    void clear();
}
