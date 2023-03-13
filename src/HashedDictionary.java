import java.util.Iterator;
import java.util.NoSuchElementException;


public class HashedDictionary<K, V> implements DictionaryInterface<K, V> {
	// The dictionary:
	private int numberOfEntries;
	private static final int DEFAULT_CAPACITY = 7; // Must be prime
	private static final int MAX_CAPACITY = 10000;
	
	// The hash table:
	private TableEntry<K, V>[] hashTable;
	private int tableSize; // Must be prime
	private static final int MAX_SIZE = 2 * MAX_CAPACITY;
	private boolean initialized = false;
	private static final double MAX_LOAD_FACTOR = 0.8; // Fraction of hash table
	public int prime=0;
	public int ct=1;//	If the value is 1, it is linear, if it is 2, it is double.
	public int hf=1;// If the value is 1, it is SSF, if it is 2, it is PAF.
	public int collision_count=0;
	// that can be filled

	public HashedDictionary() {
		this(DEFAULT_CAPACITY); // Call next constructor
	} // end default constructor

	public HashedDictionary(int initialCapacity) {
		checkCapacity(initialCapacity);
		numberOfEntries = 0; // Dictionary is empty
		
		// Set up hash table:
		// Initial size of hash table is same as initialCapacity if it is prime;
		// otherwise increase it until it is prime size
		int tableSize = getNextPrime(initialCapacity);
		checkSize(tableSize); // Check for max array size

		// The cast is safe because the new array contains null entries
		@SuppressWarnings("unchecked")
		TableEntry<K, V>[] temp = (TableEntry<K, V>[]) new TableEntry[tableSize];
		hashTable = temp;
		prime=getPreviousPrime(tableSize-1);
		
		initialized = true;
	} // end constructor

	public V getValue(K key) {
		checkInitialization();
		V result = null;
		
		int index = getHashIndex(key);
		
		int index2 = getHashIndex2(key);
		
		if(ct==2) {
			index += index2;
			index %= hashTable.length;
		}
	
	
		index = locate(index,index2, key);
		
		if (index != -1)
			result = hashTable[index].getValue(); // Key found; get value
		// Else key not found; return null
		return result;
	}

	public V remove(K key) {
		checkInitialization();
		V removedValue = null;
		int index = getHashIndex(key);
		int index2 = getHashIndex2(key);
		
		if(ct==2) {
			index += index2;
			index %= hashTable.length;
		}
		
		index = locate(index,index2, key);
		if (index != -1) { // Key found; flag entry as removed and return its value
			removedValue = hashTable[index].getValue();
			hashTable[index].setToRemoved();
			numberOfEntries--;
		} // end if
			// Else key not found; return null
		return removedValue;
	}

	private int locate(int index,int index2 ,K key) {
		boolean found = false;
		while (!found && (hashTable[index] != null)) {
			if (hashTable[index].isIn() && key.equals(hashTable[index].getKey()))
				found = true; // Key found
			else // Follow LP sequence
				if (ct==2) {
					index += index2;
					index %= hashTable.length;
					
				}else {
					index = (index + 1) % hashTable.length; // Linear probing
				}
				
		} // end while
			// Assertion: Either key or null is found at hashTable[index]
		int result = -1;
		if (found)
			result = index;
		return result;
	}

	public void Search(K key)
	{
		V value = getValue(key);
		if(value != null) {
			String list=getValue(key).toString();
			String[] arrOfStr = list.split("\n");
			
			System.out.println( ">Search: "+key+"\n"+arrOfStr.length+" documents found!" +"\n"+list);
		}else {
			System.out.println( ">Search: "+key +"\n"+"Not found!");
		}
		
		
	} // end displayHashTable
	
	public void Transferring(HashedDictionary hs2,String str)
	{
		checkInitialization();
		for (int index = 0; index < hashTable.length; index++)
		{
			if (hashTable[index] == null)
				System.out.print("");
			else if (hashTable[index].isRemoved())
				System.out.println("");
			else
				if (hs2.contains(hashTable[index].getKey())) {
					hs2.put(hashTable[index].getKey(), hs2.getValue(hashTable[index].getKey())+"\n"+hashTable[index].getValue()+"-"+str);
				}else
					hs2.put(hashTable[index].getKey(), hashTable[index].getValue()+"-"+str);
				
		} // end for
		
	} // end displayHashTable

	// -------------------------
	
	public V put(K key, V value) {
		checkInitialization();
		if ((key == null) || (value == null))
			throw new IllegalArgumentException();
		else {
			V oldValue; // Value to return
			int index = getHashIndex(key);
			int index2 = getHashIndex2(key);
			
			// Check for and resolve collision
			if (ct==2) {
				index += index2;
				index %= hashTable.length;
				index = DH(index,index2, key);
			}else {
				index = LP(index, key);
			}
			
						
			// Assertion: index is within legal range for hashTable
			assert (index >= 0) && (index < hashTable.length);
			if ((hashTable[index] == null) || hashTable[index].isRemoved()) { // Key not found, so insert new entry
				hashTable[index] = new TableEntry<>(key, value);
				numberOfEntries++;
				oldValue = null;
			} else { // Key found; get old value for return and then replace it
				oldValue = hashTable[index].getValue();				
				hashTable[index].setValue(value);
			} // end if
				// Ensure that hash table is large enough for another put
			if (isHashTableTooFull())
				resize();
			return oldValue;
		} // end if
	}

	private void resize() {
		TableEntry<K, V>[] oldTable = hashTable;
		int oldSize = hashTable.length;
		int newSize = getNextPrime(oldSize + oldSize);
		prime=getPreviousPrime(newSize-1);
		
		// The cast is safe because the new array contains null entries
		@SuppressWarnings("unchecked")
		TableEntry<K, V>[] temp = (TableEntry<K, V>[]) new TableEntry[newSize];
		hashTable = temp;
		numberOfEntries = 0; // Reset number of dictionary entries, since
		// it will be incremented by put during rehash
		// Rehash dictionary entries from old array to the new and bigger
		// array; skip both null locations and removed entries
		for (int index = 0; index < oldSize; index++) {
			if ((oldTable[index] != null) && oldTable[index].isIn())
				put(oldTable[index].getKey(), oldTable[index].getValue());
		} // end for
	}

	private int LP(int index, K key) {
		boolean found = false;
		int removedStateIndex = -1; // Index of first location in removed state
		while (!found && (hashTable[index] != null)) {
			if (hashTable[index].isIn()) {
				if (key.equals(hashTable[index].getKey()))
					found = true; // Key found
				else { // Follow LP sequence
					index = (index + 1) % hashTable.length; // Linear probing
					collision_count++;}
			} else // Skip entries that were removed
			{
				// Save index of first location in removed state
				if (removedStateIndex == -1)
					removedStateIndex = index;
					index = (index + 1) % hashTable.length; // Linear probing
					collision_count++;
			} // end if
		} // end while
			// Assertion: Either key or null is found at hashTable[index]
		if (found || (removedStateIndex == -1))
			return index; // Index of either key or null
		else
			return removedStateIndex; // Index of an available location
	}
	
	private int DH(int index,int index2, K key) {
		boolean found = false;
		int removedStateIndex = -1; // Index of first location in removed state
		while (!found && (hashTable[index] != null)) {
			if (hashTable[index].isIn()) {
				if (key.equals(hashTable[index].getKey()))
					found = true; // Key found
				else { // Follow LP sequence					
			        index += index2;
			        index %= hashTable.length;
			        collision_count++;
				}					
										
			} else // Skip entries that were removed
			{
				// Save index of first location in removed state
				if (removedStateIndex == -1)
					removedStateIndex = index;
				index += index2;
			    index %= hashTable.length;
			    collision_count++;
			} // end if
		} // end while
			// Assertion: Either key or null is found at hashTable[index]
		if (found || (removedStateIndex == -1))
			return index; // Index of either key or null
		else
			return removedStateIndex; // Index of an available location
	}
	
	
	
	
	
	

	private void checkInitialization()
	{
		if (!initialized)
			throw new SecurityException("HashedDictionary object is not initialized properly.");
	} // end checkInitialization

	// Ensures that the client requests a capacity

	// that is not too small or too large.

	private void checkCapacity(int capacity)
	{
		if (capacity < DEFAULT_CAPACITY)
			capacity = DEFAULT_CAPACITY;
		else if (capacity > MAX_CAPACITY)
			throw new IllegalStateException("Attempt to create a dictionary " +
					"whose capacity is larger than " +
					MAX_CAPACITY);
	} // end checkCapacity
	// Throws an exception if the hash table becomes too large.
	private void checkSize(int size)
	{
		if (tableSize > MAX_SIZE)
			throw new IllegalStateException("Dictionary has become too large.");
	} // end checkSize

	
	
	
	private boolean isHashTableTooFull()
	{
		return numberOfEntries > MAX_LOAD_FACTOR * hashTable.length;
	}

	private int getPreviousPrime(int integer) {
		if (integer % 2 == 0) {
			integer--;
		}
		while (!isPrime(integer)) {
			integer = integer - 2;
		}
		return integer;
	}
	
	private int getNextPrime(int integer)
	{
		// if even, put 1 to make odd
		if (integer % 2 == 0)
		{
			integer++;
		} // end if
		// test odd integers
		while (!isPrime(integer))
		{
			integer = integer + 2;
		} // end while
		return integer;
	} // end getNextPrime

	private boolean isPrime(int integer)
	{
		boolean result;
		boolean done = false;
		// 1 and even numbers are not prime
		if ((integer == 1) || (integer % 2 == 0))
		{
			result = false;
		}
		// 2 and 3 are prime
		else if ((integer == 2) || (integer == 3))
		{
			result = true;
		}
		else // integer is odd and >= 5
		{
			assert (integer % 2 != 0) && (integer >= 5);
			// a prime is odd and not divisible by every odd integer up to its square root
			result = true; // assume prime
			for (int divisor = 3; !done && (divisor * divisor <= integer); divisor = divisor + 2)
			{
				if (integer % divisor == 0)
				{
					result = false; // divisible; not prime
					done = true;
				} // end if
			} // end for
		} // end if
		return result;
	} // end isPrime

	private class KeyIterator implements Iterator<K> {
		private int currentIndex; // Current position in hash table
		private int numberLeft; // Number of entries left in iteration

		private KeyIterator() {
			currentIndex = 0;
			numberLeft = numberOfEntries;
		} // end default constructor

		public boolean hasNext() {
			return numberLeft > 0;
		} // end hasNext

		public K next() {
			K result = null;
			if (hasNext()) {
				// Skip table locations that do not contain a current entry
				while ((hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved()) {
					currentIndex++;
				} // end while
				result = hashTable[currentIndex].getKey();
				numberLeft--;
				currentIndex++;
			} else
				throw new NoSuchElementException();
			return result;
		} // end next

		public void remove() {
			throw new UnsupportedOperationException();
		} // end remove
	}

	private class ValueIterator implements Iterator<V> {
		private int currentIndex;
		private int numberLeft;

		private ValueIterator() {
			this.currentIndex = 0;
			this.numberLeft = numberOfEntries;
		}

		public boolean hasNext() {
			return this.numberLeft > 0;
		}

		public V next() {
			V result = null;
			if (hasNext()) {
				while ((hashTable[this.currentIndex] == null) || hashTable[this.currentIndex].isRemoved()) {
					this.currentIndex++;
				}
				result = hashTable[this.currentIndex].getValue();
				this.numberLeft--;
				this.currentIndex++;
			} else
				throw new NoSuchElementException();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	private int SSF(K key) {
		int hashIndex = 0;
		  for(int i = 0; i < key.toString().length(); i++)
		    {
		        hashIndex = (hashIndex + (key.toString().charAt(i) -
		                    'a' + 1));
		    }
		  return hashIndex;
	}
	private int PAF(K key) {
		int hashIndex = 0;
		int prm=31;
		  for(int i = 0; i < key.toString().length(); i++)
		    {
		        hashIndex = (hashIndex + ((key.toString().charAt(i) -
		                    'a' + 1) * (int)Math.pow(prm, key.toString().length()-1-i)));
		    }
		  return hashIndex;
	}
	
	
	
	
	private int getHashIndex(K key) {
		int hashIndex = 0;
		if(hf==1) {
			 hashIndex = SSF(key);
			
		}else {
			 hashIndex = PAF(key);
		}
		
		
		hashIndex%=hashTable.length;
		
		if (hashIndex < 0)
			hashIndex = hashIndex + hashTable.length;
		return hashIndex;
	} // end getHashIndex

	private int getHashIndex2(K key) {
		int hashIndex = 0;
		if(hf==1) {
			 hashIndex = SSF(key);
		}else {
			 hashIndex = PAF(key);
		}
		hashIndex%=hashTable.length;
		if (hashIndex < 0)
			hashIndex = hashIndex + hashTable.length;
		
		return prime - (hashIndex % prime);
	} // end getHashIndex
	

	
	public boolean contains(K key) {
		return getValue(key) != null;
	}

	public boolean isEmpty() {
		return this.numberOfEntries == 0;
	}

	public int getSize() {
		return this.numberOfEntries;
	}

	public final void clear() {
		checkInitialization();
		for (int index = 0; index < this.hashTable.length; index++)
			hashTable[index] = null;
		this.numberOfEntries = 0;
	}

	public Iterator<K> getKeyIterator() {
		return new KeyIterator();
	}

	public Iterator<V> getValueIterator() {
		return new ValueIterator();
	}

	
	private static class TableEntry<S, T>

	{
		private S key;
		private T value;
		private States state; // Flags whether this entry is in the hash table

		private enum States {
			CURRENT, REMOVED
		} // Possible values of state

		private TableEntry(S searchKey, T dataValue)

		{
			key = searchKey;
			value = dataValue;
			state = States.CURRENT;
		} // end constructor

		private S getKey() {
			return key;
		} // end getKey

		private T getValue() {
			return value;
		} // end getValue

		private void setValue(T newValue) {
			value = newValue;
		} // end setValue
			// Returns true if this entry is currently in the hash table.

		private boolean isIn() {
			return state == States.CURRENT;
		} // end isIn

		// Returns true if this entry has been removed from the hash table.

		private boolean isRemoved() {
			return state == States.REMOVED;
		} // end isRemoved
			// Sets the state of this entry to removed.

		private void setToRemoved() {
			key = null;
			value = null;
			state = States.REMOVED; // Entry not in use, ie deleted from table
		} // end setToRemoved
			// Sets the state of this entry to current.

		private void setToIn() // Not used
		{
			state = States.CURRENT; // Entry in use
		} // end setToIn
	} // end TableEntry

	
	
	
	
} // end HashedDictionary