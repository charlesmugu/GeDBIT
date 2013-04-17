package GeDBIT.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

import GeDBIT.type.Table;
import GeDBIT.util.MckoiObjectIOManager;
import GeDBIT.util.ObjectIOManager;

public class TableManager implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6911571514920379964L;

    private static TableManager tableManager;

    private int count;

    private Hashtable<String, Integer> tableAddresses;

    private Hashtable<Integer, Long> oiomLocations;

    transient private Hashtable<Integer, Table> tables;

    transient protected ObjectIOManager oiom;
    private String indexPrefix;

    private TableManager(String prefix) {
	tableAddresses = new Hashtable<String, Integer>();
	oiomLocations = new Hashtable<Integer, Long>();
	tables = new Hashtable<Integer, Table>();
	this.indexPrefix = prefix;
    }

    public static TableManager getTableManager(String prefix) {
	if (tableManager == null) {
	    if (new File(prefix + "-GeDBIT.db").exists()) {
		ObjectInputStream objectStream;
		try {
		    objectStream = new ObjectInputStream(new FileInputStream(
			    prefix + "-GeDBIT.db"));
		    tableManager = (TableManager) objectStream.readObject();
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
		}
	    } else {
		tableManager = new TableManager(prefix);
	    }
	    tableManager.open();
	}
	return tableManager;
    }

    public void open() {
	initOIOM(false);
	openOIOM();
    }

    protected void initOIOM(boolean readOnly) {
	oiom = new MckoiObjectIOManager(this.indexPrefix + "-GeDBIT", "000",
		1024 * 1024 * 1024, "Java IO", 4, 128 * 1024, readOnly);
    }

    protected void openOIOM() {
	try {
	    if (!oiom.open()) {
		throw new Error("Cannot open store for TableManager"
			+ this.indexPrefix + "-GeDBIT.000");
	    }
	    // System.out.println("OIOM.size = " + oiom.size() + "\n");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public Table getTable(String indexName) {
	Integer tableLocation = tableAddresses.get(indexName);

	Table table = null;
	// if the table exists
	if (tableLocation != null) {
	    double startTime, endTime, runTime;
	    System.out
		    .println("The table already exists. No new table is created.");
	    System.out.println("Deserializing table...");
	    startTime = System.currentTimeMillis();
	    try {
		table = (Table) oiom.readObject(oiomLocations
			.get(tableLocation));
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    } catch (InstantiationException e) {
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    }

	    endTime = System.currentTimeMillis();
	    runTime = (endTime - startTime) / 1000.00;
	    System.out.println("Time to deserialize table: " + runTime);
	}
	return table;
    }

    public Table getTable(int tableIndex) {
	if (!tables.containsKey(tableIndex)) {
	    try {
		tables.put(tableIndex,
			(Table) oiom.readObject(oiomLocations.get(tableIndex)));
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    } catch (InstantiationException e) {
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    }
	}
	return tables.get(tableIndex);
    }

    public synchronized int getLocation() {
	count++;
	return count;
    }

    public long size() {
	try {
	    return oiom.size();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return -1;
    }

    public long putTable(Table table, int tableLocation) {
	Long pointer = null;
	try {
	    pointer = oiom.writeObject(table);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	// now add this address
	tableAddresses.put(table.getIndexPrefix(), tableLocation);
	oiomLocations.put(tableLocation, pointer);

	return pointer;
    }

    protected void finalize() throws Throwable {
	try {
	    close(); // close open files
	} finally {
	    super.finalize();
	}
    }

    public void close() {
	try {
	    ObjectOutputStream out = new ObjectOutputStream(
		    new FileOutputStream(this.indexPrefix + "-GeDBIT.db"));
	    out.writeObject(tableManager);
	    out.flush();
	    out.close();
	    oiom.close();
	    tableManager = null;
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
	    ClassNotFoundException {
	in.defaultReadObject();
	tables = new Hashtable<Integer, Table>();
    }

    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }
}