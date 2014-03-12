/**
 * 
 */
package com.chamago.test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.regionserver.Store;
import org.apache.hadoop.hbase.regionserver.assistant.QualifierAS2RowValueAssistant;
import org.apache.hadoop.hbase.regionserver.assistant.RowValueSwapAssistant;
import org.apache.hadoop.hbase.util.Bytes;


/**
 * @author Gavin.peng
 * 
 *         2013-7-22 下午04:26:03 × copapi-platform-hbase
 */
public class TestAssistantStore {

	public void testAssistantStore() {
	}

	private HRegion region = null;
	private Configuration conf = HBaseConfiguration.create();
	private static byte[] ROW = Bytes.toBytes("testRow");
	private static final int ROWSIZE = 200;
	private static byte[][] ROWS = makeN(ROW, ROWSIZE);
	private static byte[] QUAL = Bytes.toBytes("testQual");
	private static final int QUALSIZE = 3;
	private static byte[][] QUALS = makeN(QUAL, QUALSIZE);
	private static byte[] VALUE = Bytes.toBytes("testValue");
	private static final int VALUESIZE = 10;
	private static byte[][] VALUES = makeN(VALUE, VALUESIZE);

	public void testRowValueSwapAssistant() throws IOException {
		String method = "testRowValueSwapAssistant";
		byte[] regionStartKey = ROW;
		byte[] regionEndKey = Bytes.add(ROW, new byte[] { (byte) 0xff });
		byte[] tableName = Bytes.toBytes(method);
		byte[] normalFamilyName = Bytes.toBytes("family");
		byte[] assistantFamilyName = Bytes.toBytes("assistantFamily");
		HColumnDescriptor normalFamily = new HColumnDescriptor(normalFamilyName);
		HColumnDescriptor assistantFamily = new HColumnDescriptor(
				assistantFamilyName).setAssistant(RowValueSwapAssistant
				.getAssistantConfString(null, null));
		this.region = initHRegion(tableName, regionStartKey, regionEndKey,
				method, conf, normalFamily, assistantFamily);
		Store normalStore = this.region.getStore(normalFamilyName);
		Store assistantStore = this.region.getStore(assistantFamilyName);
		System.out.println(normalStore.getColumnFamilyName());
		System.out.println(assistantStore.getColumnFamilyName());
		if(normalStore.isAssistant()){
			System.out.println(normalStore.getColumnFamilyName()+" is not a assistant store");
		}
		if(assistantStore.isAssistant()){
			System.out.println(assistantStore.getColumnFamilyName()+" is not a assistant store");
		}
		// assertFalse(normalStore.isAssistant());
		// assertTrue(assistantStore.isAssistant());
		// //
		// // // No data now
		// // assertNull(normalStore.getSplitPoint());
		// // assertNull(assistantStore.getSplitPoint());
		//
		// // Load data to region
		loadDataToRegion(region, normalFamilyName);
		//
		// // Check the data in normal store
		// Scan scan = new Scan();
		// // Open the scanner, check the count
		// RegionScanner scanner = region.getScanner(scan);
		// verifyCount(scanner, ROWSIZE * QUALSIZE, ROWSIZE);
		// // Check the data in normal store
		//
		// // Check the data in assistant store
		// scan = new Scan();
		// scan.addFamily(assistantFamilyName);
		// scanner = region.getScanner(scan);
		// verifyCount(scanner, ROWSIZE * QUALSIZE, ROWSIZE);
		//
		// // Check the data with filter
		// byte[] foundValue = VALUES[0];
		// Filter filter = new ValueFilter(CompareOp.EQUAL, new
		// BinaryComparator(
		// foundValue ));
		// int expectRowCount = ROWSIZE / VALUESIZE
		// + ((ROWSIZE % VALUESIZE) == 0 ? 0 : 1);
		// scan = new Scan().setFilter(filter);
		// scanner = region.getScanner(scan);
		// verifyCount(scanner, expectRowCount * QUALSIZE, expectRowCount);
		//
		// // Using assistant store to scan the equal data without filter
		// scan = new Scan().setAssistantScan(new Scan().setStartRow(foundValue)
		// .setStopRow(Bytes.add(foundValue, new byte[] { (byte) 0xff })));
		// scanner = region.getScanner(scan);
		// verifyCount(scanner, expectRowCount * QUALSIZE, expectRowCount);
		//
		// // Delete some rows
		// int deleteCount = 10;
		// //assertTrue(deleteCount < ROWSIZE);
		//
		// for (int i = 0; i < deleteCount; i++) {
		// Delete delete = new Delete(ROWS[i]);
		// region.delete(delete, null, true);
		// }
		//
		// // check after deleting
		// scan = new Scan();
		// scanner = region.getScanner(scan);
		// verifyCount(scanner, (ROWSIZE - deleteCount) * QUALSIZE,
		// (ROWSIZE - deleteCount));
		//
		// // Check the data in assistant store
		// scan = new Scan();
		// scan.addFamily(assistantFamilyName);
		// scanner = region.getScanner(scan);
		// verifyCount(scanner, (ROWSIZE - deleteCount) * QUALSIZE,
		// (ROWSIZE - deleteCount));

	}

	private void verifyCount(InternalScanner scanner, int expectedKVCount,
			int expectedRowCount) throws IOException {
		List<KeyValue> kvList = new ArrayList<KeyValue>();
		int rowCount = 0;
		int kvCount = 0;
		try {
			while (scanner.next(kvList)) {
				if (kvList.isEmpty())
					continue;
				rowCount++;
				kvCount += kvList.size();
				kvList.clear();
			}
		} finally {
			scanner.close();
		}
		if (!kvList.isEmpty()) {
			rowCount++;
			kvCount += kvList.size();
			kvList.clear();
		}
		// assertEquals(expectedKVCount, kvCount);
		// assertEquals(expectedRowCount, rowCount);
	}

	private static void loadDataToRegion(HRegion region, byte[] family)
			throws IOException {
		for (int i = 0; i < ROWSIZE; i++) {
			Put put = new Put(ROWS[i]);
			for (int j = 0; j < QUALSIZE; j++) {
				put.add(family, QUALS[j], VALUES[i % VALUESIZE]);
			}
			region.put(put);
			if (i == ROWSIZE / 3 || i == ROWSIZE * 2 / 3) {
				region.flushcache();
			}
		}
	}

	public static HRegion initHRegion(byte[] tableName, String callingMethod,
			Configuration conf, HColumnDescriptor... families)
			throws IOException {
		return initHRegion(tableName, null, null, callingMethod, conf, families);
	}

	/**
	 * @param tableName
	 * @param startKey
	 * @param stopKey
	 * @param callingMethod
	 * @param conf
	 * @param families
	 * @throws IOException
	 * @return A region on which you must call
	 *         {@link HRegion#closeHRegion(HRegion)} when done.
	 */
	private static HRegion initHRegion(byte[] tableName, byte[] startKey,
			byte[] stopKey, String callingMethod, Configuration conf,
			HColumnDescriptor... families) throws IOException {
		HTableDescriptor htd = new HTableDescriptor(tableName);
		for (HColumnDescriptor family : families) {
			htd.addFamily(family);
		}
		String DIR = "";
		HRegionInfo info = new HRegionInfo(htd.getName(), startKey, stopKey,
				false);
		Path path = new Path(DIR + callingMethod);
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(path)) {
			if (!fs.delete(path, true)) {
				throw new IOException("Failed delete of " + path);
			}
		}
		return HRegion.createHRegion(info, path, conf, htd);
	}

	private static byte[][] makeN(byte[] base, int n) {
		byte[][] ret = new byte[n][];
		for (int i = 0; i < n; i++) {
			ret[i] = Bytes.add(base, Bytes.toBytes(String.format("%04d", i)));
		}
		return ret;
	}

	public void testRowValueSwapAssistantScan() throws IOException,
			InterruptedException {

		Configuration cfg = HBaseConfiguration.create();
		byte[] tableName = Bytes.toBytes("testRowValueSwapAssistant");
		byte[] normalFamilyName = Bytes.toBytes("family");
		byte[] assistantFamilyName = Bytes.toBytes("assistantFamily");
		byte[] qualifier = Bytes.toBytes("qualifier");
//		 HBaseAdmin admin = new HBaseAdmin(cfg);
//		 HColumnDescriptor normalFamily = new
//		 HColumnDescriptor(normalFamilyName);
//		 HColumnDescriptor assistantFamily = new HColumnDescriptor(
//		 assistantFamilyName).setAssistant(RowValueSwapAssistant
//		 .getAssistantConfString("family", "qualifier"));
//		//
//		 HTableDescriptor desc = new HTableDescriptor(tableName);
//		 desc.addFamily(normalFamily);
//		 desc.addFamily(assistantFamily);
//		 admin.createTable(desc);
		// TEST_UTIL.getHBaseAdmin().createTable(desc);
		//
		// // // Load data to table
		HTable htable = new HTable(cfg, tableName);
//		Delete d5 = new Delete(Bytes.toBytes("testrow0005"));
//		Delete d4 = new Delete(Bytes.toBytes("testrow0004"));
//		List<Delete> dl = new ArrayList<Delete>();
//		dl.add(d4);
//		dl.add(d5);
//		htable.delete(dl);
		int rowCount = 50;
		int valueCount = 10;
//		 for (int i = 0; i < rowCount; i++) {
//			 Put put = new Put(Bytes.toBytes("testrow" + String.format("%04d",
//			 i)));
//			 put.add(normalFamilyName, qualifier,
//			 Bytes.toBytes("testvalue" + String.format("%04d", i % valueCount)));
//			 htable.put(put);
//		 }
		//
		// // verify count
//		 Scan scan = new Scan();
//		 //scan.addColumn(assistantFamilyName, qualifier);
//		 //scan.setAttribute(Scan.ASSISTANT_QUALIFIER, null);
//		 scan.setCaching(100);
////		 scan.addFamily(normalFamilyName);
////		 scan.addFamily(assistantFamilyName);
////		 scan.setStartRow(Bytes.toBytes("testrow0005"));
////		 scan.setStopRow(Bytes.toBytes("testrow0006"));
//		 ResultScanner scanner = htable.getScanner(scan);
//		 verifyCount(scanner, rowCount, rowCount,null);
		//
		// // verify count with assistant scan
//		Scan as = new Scan();
//		as.setCaching(10);
//		as.addFamily(assistantFamilyName);
//		as.setAttribute(Scan.ASSISTANT_QUALIFIER, Bytes.toBytes("qualifier"));
//		as.setStartRow(Bytes.toBytes("testvalue0001testrow0001"));
//		as.setStopRow(Bytes.toBytes("testvalue0005testrow0005"));
//		Scan scan = new Scan().setAssistantScan(as);
////		scan.setStartRow(Bytes.toBytes("testvalue0001testrow0001"));
////		scan.setStopRow(Bytes.toBytes("testvalue0005testrow0005"));
//		ResultScanner scanner = htable.getScanner(scan);
//		verifyCount(scanner, rowCount, rowCount, normalFamilyName);

		 //split region
//		 HBaseAdmin admin = new HBaseAdmin(cfg);
		 byte[] splitPoint = Bytes.toBytes("testrow"
		 + String.format("%04d", 20));
//		 admin.split(tableName, splitPoint);
//		
//		 // wait unit region split is done
//		 long timeout = System.currentTimeMillis() + (15 * 1000);
//		 while ((System.currentTimeMillis() < timeout)
//		 && (htable.getRegionLocations().size() < 2)) {
//		 Thread.sleep(250);
//		 }
		//
		 System.out.println(Bytes.toString(splitPoint));
		 Scan as = new Scan();
		// as.setStopRow(Bytes.toBytes("testvalue0005testrow0005"));
		 //as.setStartRow(Bytes.toBytes("testvalue0002testrow0002"));
		as.addFamily(assistantFamilyName);
		as.setAttribute(Scan.ASSISTANT_QUALIFIER, Bytes.toBytes("qualifier"));
		//as.set
		//Scan scan = new Scan().setAssistantScan(as);
		// 如果主scan也设置了stopKey，那么在跨region scan时会生效,如果设置了startRow，则在定位region时受影响;
		Scan scan = new Scan().setStartRow(Bytes.toBytes("testrow0025")).setAssistantScan(as);
		scan.setCaching(50);
		ResultScanner scanner = htable.getScanner(scan);
		 verifyCount(scanner, rowCount / 2, rowCount / 2,null);
		//
		// // scan the table with condition, using the assistant scan
//		 Scan assistantScan = new Scan();
//		 assistantScan.addFamily(assistantFamilyName);
//		 assistantScan.addColumn(assistantFamilyName, qualifier);
//		 byte[] column = assistantScan.getFamilyMap().get(assistantFamilyName).first();
//		 System.out.println(Bytes.toString(column));
//		 byte[] assistantStart = Bytes.toBytes("testvalue" +
//		 String.format("%04d", 0));
//		 byte[] assistantStop = Bytes.toBytes("testvalue" +
//		 String.format("%04d", valueCount / 2));
//		 assistantScan.setStartRow(assistantStart);
//		 assistantScan.setStopRow(assistantStop);
//		 Scan scan = new Scan().setAssistantScan(assistantScan);
//		 ResultScanner scanner = htable.getScanner(scan);
//		 verifyCount(scanner,rowCount, rowCount,null);
		// int scannedRowCount = 0;
		// Result result = null;
		// while ((result = scanner.next()) != null) {
		// scannedRowCount++;
		// // assertTrue(Bytes.compareTo(result.getRow(), assistantStart) >= 0);
		// // assertTrue(Bytes.compareTo(result.getRow(), assistantStop) < 0);
		// System.out.println(result);
		// }
		 //scanner.close();
		// assertEquals(rowCount / 2, scannedRowCount);

	}

	private static void verifyCount(ResultScanner scanner, int expectedKVCount,
			int expectedRowCount, byte[] familyName) throws IOException {
		int rowCount = 0;
		int kvCount = 0;
		try {
			Result result = null;
			while ((result = scanner.next()) != null) {
				rowCount++;
				System.out.println("rowkey:" + Bytes.toString(result.getRow())
						+",mt:"+Bytes.toString(result.getValue(Bytes.toBytes("Trade"), Bytes.toBytes("ModifiedTime"))));
				kvCount += result.size();
				// result.g
				// System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("af"),
				// Bytes.toBytes("rk"))));
				// System.out.println(Bytes.toString(result.getRow()));
			}
		} finally {
			scanner.close();
		}
		System.out.println("expectedKVCount:" + kvCount);
		System.out.println("expectedRowCount:" + rowCount);
		// assertEquals(expectedKVCount, kvCount);
		// assertEquals(expectedRowCount, rowCount);
	}

	public void testQualifierAssistantScan() throws IOException,
	InterruptedException {

		Configuration cfg = HBaseConfiguration.create();
		byte[] tableName = Bytes.toBytes("testRowValueSwapAssistant");
		byte[] normalFamilyName = Bytes.toBytes("family");
		byte[] assistantFamilyName = Bytes.toBytes("assistantFamily");
		byte[] qualifier = Bytes.toBytes("qualifier");

		createTable();
		//
		// // // Load data to table
		HTable htable = new HTable(cfg, tableName);
		//Delete d5 = new Delete(Bytes.toBytes("testrow0005"));
		//Delete d4 = new Delete(Bytes.toBytes("testrow0004"));
		//List<Delete> dl = new ArrayList<Delete>();
		//dl.add(d4);
		//dl.add(d5);
		//htable.delete(dl);
		int rowCount = 500;
		int valueCount = 10;
		 for (int i = 0; i < rowCount; i++) {
			 Put put = new Put(Bytes.toBytes("testrow" + String.format("%04d",
			 i)));
			 put.add(normalFamilyName, qualifier,
			 Bytes.toBytes("testvalue" + String.format("%04d", i % valueCount)));
			 htable.put(put);
		 }
		//
		// // verify count
		// Scan scan = new Scan();
		// //scan.addColumn(assistantFamilyName, qualifier);
		// //scan.setAttribute(Scan.ASSISTANT_QUALIFIER, null);
		// scan.setCaching(100);
		//// scan.addFamily(normalFamilyName);
		//// scan.addFamily(assistantFamilyName);
		//// scan.setStartRow(Bytes.toBytes("testrow0005"));
		//// scan.setStopRow(Bytes.toBytes("testrow0006"));
		// ResultScanner scanner = htable.getScanner(scan);
		// verifyCount(scanner, rowCount, rowCount,null);
		//
		// // verify count with assistant scan
		//Scan as = new Scan();
		//as.setCaching(10);
		//as.addFamily(assistantFamilyName);
		//as.setAttribute(Scan.ASSISTANT_QUALIFIER, Bytes.toBytes("qualifier"));
		//as.setStartRow(Bytes.toBytes("testvalue0001testrow0001"));
		//as.setStopRow(Bytes.toBytes("testvalue0005testrow0005"));
		//Scan scan = new Scan().setAssistantScan(as);
		////scan.setStartRow(Bytes.toBytes("testvalue0001testrow0001"));
		////scan.setStopRow(Bytes.toBytes("testvalue0005testrow0005"));
		//ResultScanner scanner = htable.getScanner(scan);
		//verifyCount(scanner, rowCount, rowCount, normalFamilyName);
		
		 //split region
		
//		 Scan as = new Scan();
//		// as.setStopRow(Bytes.toBytes("testvalue0005testrow0005"));
//		 //as.setStartRow(Bytes.toBytes("testvalue0002testrow0002"));
//		as.addFamily(assistantFamilyName);
//		as.setAttribute(Scan.ASSISTANT_QUALIFIER, Bytes.toBytes("qualifier"));
//		//as.set
//		//Scan scan = new Scan().setAssistantScan(as);
//		// 如果主scan也设置了stopKey，那么在跨region scan时会生效,如果设置了startRow，则在定位region时受影响;
//		Scan scan = new Scan().setStartRow(Bytes.toBytes("testrow0025")).setAssistantScan(as);
//		scan.setCaching(50);
//		ResultScanner scanner = htable.getScanner(scan);
//		 verifyCount(scanner, rowCount / 2, rowCount / 2,null);
		//
		// // scan the table with condition, using the assistant scan
		// Scan assistantScan = new Scan();
		// assistantScan.addFamily(assistantFamilyName);
		// assistantScan.addColumn(assistantFamilyName, qualifier);
		// byte[] column = assistantScan.getFamilyMap().get(assistantFamilyName).first();
		// System.out.println(Bytes.toString(column));
		// byte[] assistantStart = Bytes.toBytes("testvalue" +
		// String.format("%04d", 0));
		// byte[] assistantStop = Bytes.toBytes("testvalue" +
		// String.format("%04d", valueCount / 2));
		// assistantScan.setStartRow(assistantStart);
		// assistantScan.setStopRow(assistantStop);
		// Scan scan = new Scan().setAssistantScan(assistantScan);
		// ResultScanner scanner = htable.getScanner(scan);
		// verifyCount(scanner,rowCount, rowCount,null);
		// int scannedRowCount = 0;
		// Result result = null;
		// while ((result = scanner.next()) != null) {
		// scannedRowCount++;
		// // assertTrue(Bytes.compareTo(result.getRow(), assistantStart) >= 0);
		// // assertTrue(Bytes.compareTo(result.getRow(), assistantStop) < 0);
		// System.out.println(result);
		// }
		 //scanner.close();
		// assertEquals(rowCount / 2, scannedRowCount);

	}
	
	public static void split() throws IOException, ZooKeeperConnectionException, Exception{
		Configuration cfg = HBaseConfiguration.create();
		byte[] tableName = Bytes.toBytes("QualifierAssistant");
		HBaseAdmin admin = new HBaseAdmin(cfg);
		 byte[] splitPoint = Bytes.toBytes("ec101#1807#testrow"+ String.format("%04d", 30));
		 admin.split(tableName, splitPoint);
		 //admin.sp
		 // wait unit region split is done
		 HTable htable = new HTable(cfg, tableName);
		 long timeout = System.currentTimeMillis() + (15 * 1000);
		 while ((System.currentTimeMillis() < timeout)
		 && (htable.getRegionLocations().size() < 2)) {
		 Thread.sleep(250);
		 }
		
		 System.out.println(Bytes.toString(splitPoint));
	}
	
	public static void put() throws IOException{
		
		Configuration cfg = HBaseConfiguration.create();
		byte[] tableName = Bytes.toBytes("QualifierAssistant");
		byte[] normalFamilyName = Bytes.toBytes("Trade");
		byte[] assistantCreatedFamilyName = Bytes.toBytes("CreatedAF");
		byte[] assistantModifiedFamilyName = Bytes.toBytes("ModifiedAF");
		byte[] qualifierCreated = Bytes.toBytes("created");
		byte[] qualifierModified = Bytes.toBytes("modified");
		//
		// // // Load data to table
		HTable htable = new HTable(cfg, tableName);
		//Delete d5 = new Delete(Bytes.toBytes("testrow0005"));
		//Delete d4 = new Delete(Bytes.toBytes("testrow0004"));
		//List<Delete> dl = new ArrayList<Delete>();
		//dl.add(d4);
		//dl.add(d5);
		//htable.delete(dl);
		int rowCount = 100;
		int valueCount = 10;
		 for (int i = 0; i < rowCount; i++) {
			 Put put = new Put(Bytes.toBytes("ec101#1807#testrow" + String.format("%04d",
			 i)));
			 put.add(normalFamilyName, qualifierCreated,
			 Bytes.toBytes(DateToString(new Date(),"yyyy-MM-dd HH:mm:ss")));
			 put.add(normalFamilyName, qualifierModified,
					 Bytes.toBytes(DateToString(new Date(),"yyyy-MM-dd HH:mm:ss")));
			 htable.put(put);
			 try {
				Thread.sleep(500l);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		
	}
	
	public static String DateToString(Date date,String pattern){
		
		if(null==date){
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(pattern);
		return sdf.format(date);
		
	}

	public static void createTable() throws IOException, ZooKeeperConnectionException{
		Configuration cfg = HBaseConfiguration.create();
		byte[] tableName = Bytes.toBytes("QualifierAssistant");
		byte[] normalFamilyName = Bytes.toBytes("Trade");
		byte[] assistantCreatedFamilyName = Bytes.toBytes("CreatedAF");
		byte[] assistantModifiedFamilyName = Bytes.toBytes("ModifiedAF");
		byte[] qualifierCreated = Bytes.toBytes("created");
		byte[] qualifierModified = Bytes.toBytes("modified");
		 HBaseAdmin admin = new HBaseAdmin(cfg);
		 HColumnDescriptor normalFamily = new
		 HColumnDescriptor(normalFamilyName);
		 HColumnDescriptor assistantCFamily = new HColumnDescriptor(
				 assistantCreatedFamilyName).setAssistant(QualifierAS2RowValueAssistant
		 .getAssistantConfString("Trade", "created"));
		 HColumnDescriptor assistantMFamily = new HColumnDescriptor(
				 assistantModifiedFamilyName).setAssistant(QualifierAS2RowValueAssistant
		 .getAssistantConfString("Trade", "modified"));
		//
		 HTableDescriptor desc = new HTableDescriptor(tableName);
		 //desc.get
		 desc.addFamily(normalFamily);
		 desc.addFamily(assistantCFamily);
		 desc.addFamily(assistantMFamily);
		 admin.createTable(desc);
	}
	
	public static void createTradeTable() throws IOException, ZooKeeperConnectionException{
		Configuration cfg = HBaseConfiguration.create();
		byte[] tableName = Bytes.toBytes("Trade");
		byte[] normalFamilyName = Bytes.toBytes("Trade");
		byte[] assistantCreatedFamilyName = Bytes.toBytes("CreatedAF");
		byte[] assistantModifiedFamilyName = Bytes.toBytes("ModifiedAF");
		byte[] qualifierCreated = Bytes.toBytes("created");
		byte[] qualifierModified = Bytes.toBytes("modified");
		 HBaseAdmin admin = new HBaseAdmin(cfg);
		 HColumnDescriptor normalFamily = new
		 HColumnDescriptor(normalFamilyName);
		 HColumnDescriptor assistantCFamily = new HColumnDescriptor(
				 assistantCreatedFamilyName).setAssistant(QualifierAS2RowValueAssistant
		 .getAssistantConfString("Trade", "created"));
		 HColumnDescriptor assistantMFamily = new HColumnDescriptor(
				 assistantModifiedFamilyName).setAssistant(QualifierAS2RowValueAssistant
		 .getAssistantConfString("Trade", "modified"));
		//
		 HTableDescriptor desc = new HTableDescriptor(tableName);
		 desc.addFamily(normalFamily);
		 desc.addFamily(assistantCFamily);
		 desc.addFamily(assistantMFamily);
		 admin.createTable(desc);
	}
	
	public static void deleteTable() throws IOException, ZooKeeperConnectionException{
		Configuration cfg = HBaseConfiguration.create();
		HBaseAdmin admin = new HBaseAdmin(cfg);
		byte[] tableName = Bytes.toBytes("QualifierAssistant");
		 admin.disableTable(tableName);
		 admin.deleteTable(tableName);
	}
	public static void main(String[] args) throws Exception {
		TestAssistantStore as = new TestAssistantStore();
		//System.out.println(getNextScanStartRow("2013-07-29 12:34:25","ec101#695171870#324234232324"));
		try {
			//createTable();
			//deleteTable();
			//oldScan();
			//split();
			newScan();
			//put();
			
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void oldScan() throws IOException{
		Configuration cfg = HBaseConfiguration.create();
		byte[] tableName = Bytes.toBytes("testRowValueSwapAssistant");
		byte[] normalFamilyName = Bytes.toBytes("family");
		byte[] assistantFamilyName = Bytes.toBytes("assistantFamily");
		byte[] qualifier = Bytes.toBytes("qualifier");
//		Scan as = new Scan();
//		as.setCaching(50);
//		as.addFamily(assistantFamilyName);
//		as.setAttribute(Scan.ASSISTANT_QUALIFIER, Bytes.toBytes("qualifier"));
		Scan scan = new Scan();
		scan.addFamily(assistantFamilyName);
		scan.setCaching(50);
		long startT = System.currentTimeMillis();
		HTable htable = new HTable(cfg, tableName);
		ResultScanner scanner = htable.getScanner(scan);
		List<Get> rks = new ArrayList<Get>();
		for (Result r : scanner) {
			String tradeRowKey = Bytes.toString(r.getValue(Bytes.toBytes("assistantFamily"),Bytes.toBytes("qualifier")));
			Get tGet = new Get(Bytes.toBytes(tradeRowKey));
			rks.add(tGet);
		}
		//Result[] rs = htable.get(rks);
		long endT = System.currentTimeMillis();
		System.out.println("scan end time:"+endT);
		System.out.println("scan time:"+(endT-startT));
		
	}
	
	public static void newScan() throws IOException{
		Configuration cfg = HBaseConfiguration.create();
//		byte[] tableName = Bytes.toBytes("testRowValueSwapAssistant");
//		byte[] normalFamilyName = Bytes.toBytes("family");
//		byte[] assistantFamilyName = Bytes.toBytes("assistantFamily");
//		byte[] qualifier = Bytes.toBytes("qualifier");
		
		byte[] tableName = Bytes.toBytes("Trade");
		byte[] normalFamilyName = Bytes.toBytes("Trade");
		byte[] assistantCreatedFamilyName = Bytes.toBytes("CreatedAF");
		byte[] assistantModifiedFamilyName = Bytes.toBytes("ModifiedAF");
		byte[] qualifierCreated = Bytes.toBytes("CreatedTime");
		byte[] qualifierModified = Bytes.toBytes("ModifiedTime");
		Scan as = new Scan();
		as.setCaching(200);
		//as.addColumn(assistantCreatedFamilyName,qualifierCreated);
		as.addColumn(assistantModifiedFamilyName, qualifierModified);
		 as.setStartRow(Bytes.toBytes("ec101#695171870#130701120000#"));
		 as.setStopRow(Bytes.toBytes("ec101#695171870#130710151505-"));
		//
		//as.setAttribute(Scan.ASSISTANT_QUALIFIER, Bytes.toBytes("created"));
		Scan scan = new Scan();
		scan.setSmall(true);
		scan.setCaching(200);
		scan.setAssistantScan(as);
//		scan.setStartRow(Bytes.toBytes("ec101#1807#testrow0083"));
//		scan.setStopRow(Bytes.toBytes("ec101#1807#testrow0090"));
		//scan.addFamily(assistantModifiedFamilyName);
		long startT = System.currentTimeMillis();
		HTable htable = new HTable(cfg, tableName);
		ResultScanner scanner = htable.getScanner(scan);
		verifyCount(scanner,100,100,null);
		long endT = System.currentTimeMillis();
		System.out.println("scan end time:"+endT);
		System.out.println("scan time:"+(endT-startT));
		
	}
	
	public static String getNextScanStartRow(String modifiedTime,String row){
		 int lastIndex = row.lastIndexOf("#");
		 StringBuilder sb = new StringBuilder(row.substring(0, lastIndex));
		 long tid = Long.parseLong(row.substring(lastIndex+1));
		 sb.append("#");
		 sb.append(formatDateString(modifiedTime));
		 sb.append("#");
		 sb.append(tid+1);
		 return sb.toString();
	}
	
	public final static String formatDateString(String str){
		 try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date d = sdf.parse(str);
			sdf.applyPattern("yyMMddHHmmss");
			return sdf.format(d);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		return null;
	}
}
