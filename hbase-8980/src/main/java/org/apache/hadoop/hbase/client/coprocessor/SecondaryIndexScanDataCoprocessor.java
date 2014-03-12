package org.apache.hadoop.hbase.client.coprocessor;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.InternalScanner;

/**
 * @author Gavin.peng
 * 通过查询二级索引直接返回对应主表的记录
 * 相对查询二级索引返回到客户端，客户端再根据rowkey来查，减少一次RPC
 * 2013-7-25 下午03:50:20
 × hbase-processor
 */
public class SecondaryIndexScanDataCoprocessor extends BaseRegionObserver {
	
	@Override
	public boolean preScannerNext(final ObserverContext<RegionCoprocessorEnvironment> e,
	      final InternalScanner s, final List<Result> results,
	      final int limit, final boolean hasMore) throws IOException {
	    return hasMore;
	}

	@Override
	public boolean postScannerNext(final ObserverContext<RegionCoprocessorEnvironment> e,
	      final InternalScanner s, final List<Result> results, final int limit,
	      final boolean hasMore) throws IOException {
		//if(s instanceof RegionScannerImpl)
		s.assistantNext(results, limit);
	    return hasMore;
	}

}
