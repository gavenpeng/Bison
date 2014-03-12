/**
 * 
 */
package org.apache.hadoop.hbase.regionserver.assistant;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author Gavin.peng
 * 
 * 2013-7-26 上午10:13:18
 × hbase-8980
 */
public class QualifierAS2RowValueAssistant extends Assistant {

	private static final Pattern ARGUMENT_PATTERN = Pattern
    .compile("^FAMILY=(.*),QUALIFIER=(.*)$");
	// What family and qualifier we want to swap row, value
	private byte[] family;
	private byte[] qualifier;
	private static final String ROWKEY_SPERATOR = "#";
	private static final long PASS_TIME = 9223300000000000000l;
	public QualifierAS2RowValueAssistant(){
		
	}
	/* (non-Javadoc)
	 * @see org.apache.hadoop.hbase.regionserver.assistant.Assistant#assist(org.apache.hadoop.hbase.regionserver.HRegion, org.apache.hadoop.hbase.client.Mutation, byte[])
	 */
	@Override
	protected Collection<Mutation> assist(HRegion region, Mutation mutation,
			byte[] assistanStoreName) throws IOException {
		List<Mutation> generatedMutations = new ArrayList<Mutation>();
	    Map<byte[], List<KeyValue>> familyMap = mutation.getFamilyMap();
	    if (mutation instanceof Put) {
	      for (Map.Entry<byte[], List<KeyValue>> entry : familyMap.entrySet()) {
	        for (KeyValue kv : entry.getValue()) {
	          if (ignoreKV(kv)) {
	        	  continue;
	          }
	          byte[] assistantRK = getAssistantRowKey(kv);
	          if(assistantRK == null){
	        	  LOG.warn("the row which put table ["+region.getTableDesc().getNameAsString()+"] rowkey format is unvalid:"+Bytes.toString(kv.getRow())+",so the row not sync to assistant store");
	        	  break;
	          }
	          Put p = new Put(assistantRK);
	          p.add(assistanStoreName, kv.getQualifier(), kv.getRow());
	          generatedMutations.add(p);
	          //修改时间的索引需要删除老的记录
	          if(Bytes.toString(this.qualifier).equals("ModifiedTime")){
	        	  Mutation del = checkHasDeleteAssistantHistory(region,kv);
		          if(del!=null){
		        	  generatedMutations.add(del);
		          }  
	          }
	         
	        }
	      }
	    } else if (mutation instanceof Delete) {
	      if(familyMap.isEmpty()){
	        for (byte[] tableFamily : region.getTableDesc().getFamiliesKeys()) {
	          ((Delete) mutation).deleteFamily(tableFamily, mutation.getTimeStamp());
	        }
	      }
	      for (Map.Entry<byte[], List<KeyValue>> entry : familyMap.entrySet()) {
	        Iterator<KeyValue> kvIterator = entry.getValue().iterator();
	        while (kvIterator.hasNext()) {
	          KeyValue kv = kvIterator.next();
	          if(!kv.isDelete()) continue;
	          // Create the Get for the corresponding delete
	          Get get = new Get(kv.getRow());
	          boolean isLatestTimestamp = kv.isLatestTimestamp();
	          if (kv.getType() == KeyValue.Type.Delete.getCode()) {
	            if (ignoreKV(kv)) continue;
	            get.addColumn(kv.getFamily(), kv.getQualifier());
	            if (!isLatestTimestamp) {
	              get.setTimeStamp(kv.getTimestamp());
	            }
	          } else if (kv.getType() == KeyValue.Type.DeleteColumn.getCode()) {
	            if (ignoreKV(kv)) continue;
	            get.addColumn(kv.getFamily(), kv.getQualifier()).setMaxVersions();
	            if (!isLatestTimestamp) {
	              get.setTimeRange(0, kv.getTimestamp() + 1);
	            }
	          } else if (kv.getType() == KeyValue.Type.DeleteFamily.getCode()) {
	            if (ignoreFamily(kv.getBuffer(), kv.getFamilyOffset(),
	                kv.getFamilyLength())) {
	              continue;
	            }
	            get.addFamily(kv.getFamily()).setMaxVersions();
	            if (!isLatestTimestamp) {
	              get.setTimeRange(0, kv.getTimestamp() + 1);
	            }
	          }

	          Result result = region.get(get, null);
	          if (result.isEmpty()) {
	            kvIterator.remove();
	            continue;
	          }

	          // Create the delete on assistant store according to the result

	          boolean updateTs = true;
	          for (KeyValue existedKV : result.raw()) {
	            long tsOfExisted = existedKV.getTimestamp();
	            // specify the time stamp for delete to prevent new putting
	            byte[] tsBytes = Bytes.toBytes(tsOfExisted);
	            if (updateTs) {
	              kv.updateLatestStamp(tsBytes);
	              updateTs = false;
	            }
	            if (ignoreQualifier(existedKV.getBuffer(),
	                existedKV.getQualifierOffset(), existedKV.getQualifierLength())) {
	              continue;
	            }

	            byte[] assistantRow = this.getAssistantRowKey(existedKV);
	            Delete assistantDelete = new Delete(assistantRow);
	            assistantDelete.addDeleteMarker(new KeyValue(assistantRow,
	                assistanStoreName, existedKV.getQualifier(), tsOfExisted,
	                KeyValue.Type.Delete, existedKV.getRow()));
	            generatedMutations.add(assistantDelete);
	          }
	        }
	      }
	    }
	    return generatedMutations;
	}

  /* (non-Javadoc)
  * @see org.apache.hadoop.hbase.regionserver.assistant.Assistant#getName()
  */
  @Override
  public String getName() {
		return this.getClass().getName();
  }


  /**
   * Check whether the given KeyValue's family and qualifier is wanted
   * @param kv
   * @return true if ignore this kv.
   */
  private boolean ignoreKV(KeyValue kv) {
    if (ignoreFamily(kv.getBuffer(), kv.getFamilyOffset(),
        kv.getFamilyLength())) {
      return true;
    }
    if (ignoreQualifier(kv.getBuffer(), kv.getQualifierOffset(),
        kv.getQualifierLength())) {
      return true;
    }
    return false;
  }

  /**
   * Check whether the given family is wanted
   * @param checkFamily
   * @return true if ignore the given family.
   */
  private boolean ignoreFamily(byte[] checkFamily, int offset, int len) {
    if (this.family != null
        && !Bytes.equals(this.family, 0, this.family.length, checkFamily,
            offset, len)) {
      return true;
    }
    return false;
  }

  /**
   * Check whether the given family is wanted
   * @param checkFamily
   * @return true if ignore the given family.
   */
  private boolean ignoreQualifier(byte[] checkQualifier, int offset, int len) {
    if (this.qualifier != null && !Bytes.equals(this.qualifier, 0, this.qualifier.length,
            checkQualifier, offset, len)) {
      return true;
    }
    return false;
  }

  @Override
  public Assistant initialize(String argumentStr) {
    Matcher matcher = ARGUMENT_PATTERN.matcher(argumentStr);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Argument:" + argumentStr
          + " is not suited for " + getName());
    }
    String familyStr = matcher.group(1);
    String qualStr = matcher.group(2);
    this.family = familyStr.isEmpty() ? null : Bytes.toBytesBinary(familyStr);
    this.qualifier = qualStr.isEmpty() ? null : Bytes.toBytesBinary(qualStr);
    return this;
  }

  /**
   * Get the configuration string of QualifierAS2RowValueAssistant when setting
   * assistant in HColumnDescriptor
   * @param family
   * @param qualifier
   * @return the configuration string
   */
	public static String getAssistantConfString(String family, String qualifier) {
	    return QualifierAS2RowValueAssistant.class.getName() + "|" + "FAMILY="
	        + ((family == null || family.isEmpty()) ? "" : family) + ",QUALIFIER="
	        + ((qualifier == null || qualifier.isEmpty()) ? "" : qualifier);
	}
	  
    public byte[] getAssistantRowKey(KeyValue kv){
    	String[] rowKey = Bytes.toString(kv.getRow()).split(ROWKEY_SPERATOR);
        if(rowKey!=null&&rowKey.length==3){
      	  String value = formatDate(Bytes.toString(kv.getValue()));
	          StringBuilder assistantRK = new StringBuilder(rowKey[0]);
	          assistantRK.append(ROWKEY_SPERATOR);
	          assistantRK.append(rowKey[1]);
	          assistantRK.append(ROWKEY_SPERATOR);
	          assistantRK.append(value);
	          assistantRK.append(ROWKEY_SPERATOR);
	          assistantRK.append(rowKey[2]);
	          return Bytes.toBytes(assistantRK.toString());
        }else{
      	  return null;
        }
    }
	
    
    public Mutation checkHasDeleteAssistantHistory(HRegion region,KeyValue kv) throws IOException{
    	 Get get = new Get(kv.getRow());
    	 Result result = region.get(get, null);
    	 if(result !=null){
    		 String oldMT = Bytes.toString(result.getValue(this.family, this.qualifier));
             String curMT = Bytes.toString(kv.getValue());
             if(curMT!=null&&oldMT!=null&&!curMT.equals(oldMT)){
            	 String mainKey = Bytes.toString(kv.getRow());
            	 String[] keyPart = mainKey.split(ROWKEY_SPERATOR);
            	 if(keyPart!=null&&keyPart.length==3){
            		 StringBuilder delKey = new StringBuilder(keyPart[0]);
                	 delKey.append(ROWKEY_SPERATOR);
                	 delKey.append(keyPart[1]);
                	 delKey.append(ROWKEY_SPERATOR);
                	 delKey.append(formatDate(oldMT));
                	 delKey.append(ROWKEY_SPERATOR);
                	 delKey.append(keyPart[2]);
                	 
                	 byte[] delRow = Bytes.toBytes(delKey.toString());
                	 Delete assistantDelete = new Delete(delRow);
                	 return assistantDelete;
            	 }
            	 
             }
    	 }
    	 
    	return null;
    }
    
    public String formatDate(String str){
		try {
			if(str == null){
				str = "1700-12-30 23:59:59";
			}
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date curDate = sdf.parse(str);
			long ftt = Long.MAX_VALUE-(curDate.getTime()+PASS_TIME);
			return String.valueOf(ftt);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		return null;
	}
    
    
		
}
