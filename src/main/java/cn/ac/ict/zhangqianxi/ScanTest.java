package cn.ac.ict.zhangqianxi;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class ScanTest {

  public static void main(String[] args) throws IOException {
    Configuration conf = HBaseConfiguration.create();
    HTable table = new HTable(conf, "testIndex");
    Scan scan = new Scan();
    SingleColumnValueFilter filter = new SingleColumnValueFilter(
        Bytes.toBytes("F"), Bytes.toBytes("c1"),
        CompareFilter.CompareOp.GREATER, Bytes.toBytes((int) 50));
    scan.setFilter(filter);
    ResultScanner scanner = table.getScanner(scan);
    for (Result result : scanner) {
      System.out.println("row: "
          + Bytes.toString(result.getRow())
          + "  c1: "
              + Bytes.toInt(result.getValue(Bytes.toBytes("F"),
                  Bytes.toBytes("c1"))));
    }
    scanner.close();
  }

}
