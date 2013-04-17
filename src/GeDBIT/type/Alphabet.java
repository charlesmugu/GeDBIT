/**
 * edu.utexas.GeDBIT.type.Alphabet 2006.05.24
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Created, by Willard
 */
package GeDBIT.type;

import java.io.Serializable;

/**
 * @author Willard
 * 
 */
public class Alphabet implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -3200343173661145001L;

    Symbol[] alphabet;
    int distinctSize;

    /**
     * @param alphabet
     * @param distinctSize
     */
    public Alphabet(Symbol[] alphabet, int distinctSize) {
	this.alphabet = alphabet;
	this.distinctSize = distinctSize;
    }

    /**
     * @return
     */
    public int size() {
	return alphabet.length;
    }

    /**
     * @param index
     * @return
     */
    public Symbol get(int index) {
	return alphabet[index];
    }

    /**
     * @return
     */
    public int distinctSize() {
	return distinctSize;
    }
}
