package cn.ac.ict.zhangqianxi;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

public class CreateTable {
  private String tablename;
  private final String family = "F";
  private int regionNumber = 20;
  private long interval = 30000;
  private byte[][] regions;

  public CreateTable(String tablename, int regionNumber, long interval) {
    this.tablename = tablename;
    this.regionNumber = regionNumber;
    this.interval = interval;
    this.regions = new byte[regionNumber - 1][];
  }

  public void split_regions() {
    for (int i = 0; i < regionNumber - 1; i++) {
      regions[i] = Bytes.toBytes(String.format("%032d", (long) (i + 1)
          * interval));
      // regions[i] = Bytes.toBytes((int) (i + 1) * interval);
    }
  }

  public void buildTable() throws IOException {
    Configuration conf = HBaseConfiguration.create();
    try {

      HBaseAdmin admin = new HBaseAdmin(conf);
      // IndexedHTableDescriptor desc = new IndexedHTableDescriptor(
      // Bytes.toBytes(tablename));
      // IndexSpecification iSpec = new
      // IndexSpecification(Bytes.toBytes(tablename
      // + "index"));
      //
      // HColumnDescriptor colDesc = new
      // HColumnDescriptor(Bytes.toBytes(family));
      //
      // iSpec.addIndexColumn(colDesc, "c1", ValueType.Int, 4);
      // desc.addFamily(colDesc);
      // desc.addIndex(iSpec);
      HTableDescriptor desc = new HTableDescriptor(Bytes.toBytes(tablename));
      HColumnDescriptor colDesc = new HColumnDescriptor(Bytes.toBytes(family));
      desc.addFamily(colDesc);
      admin.createTable(desc, regions);
      System.out.println("Table Available:"
          + admin.isTableAvailable(Bytes.toBytes(tablename)));// 判断该表的是否可用
      printTableRegions(conf, tablename);
    } catch (MasterNotRunningException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ZooKeeperConnectionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void printTableRegions(Configuration conf, String tableName) {
    System.out.println("Printing regions of table: " + tableName);
    try {
      HTable table = new HTable(conf, tableName);
      Pair<byte[][], byte[][]> pair = table.getStartEndKeys();
      for (int n = 0; n < pair.getFirst().length; n++) {
        byte[] sk = pair.getFirst()[n];
        byte[] ek = pair.getSecond()[n];
        System.out.println("[" + (n + 1) + "]" + "start key:  "
            + Bytes.toString(sk) + ", end key:  " + Bytes.toString(ek));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    String tablename = null;
    int regionNumber = 0;
    long interval = 0;

    Options options = new Options();
    options.addOption("t", "tablename", true,
        "the name of the table which you want to create");// true标志，如果一个参数是必需的信号后
    options.addOption("n", "regionNumber", true,
        "the number of the region which you want to create");
    options.addOption("i", "interval", true, "the interval of each region");// interval?

    try {
      CommandLine cli = new GnuParser().parse(options, args);// parse按照特定的格式把字符串解析为日期对象?
      if (cli.hasOption("t")) {
        tablename = cli.getOptionValue("t");
      } else {
        throw new ParseException(
            "option: -t, --the name of the table which you want to create !");
      }
      if (cli.hasOption("n")) {
        regionNumber = Integer.valueOf(cli.getOptionValue("n"));
      } else {
        throw new ParseException(
            "option: -n, --the number of the region which you want to create !");
      }
      if (cli.hasOption("i")) {
        interval = Long.valueOf(cli.getOptionValue("i")).longValue();
      } else {
        throw new ParseException("option: -i, --the interval of each region !");
      }
      System.out.println("tablename:" + tablename);
      System.out.println("regionNumber:" + regionNumber);
      System.out.println("interval:" + interval);

      CreateTable ct = new CreateTable(tablename, regionNumber, interval);
      ct.split_regions();
      ct.buildTable();

    } catch (ParseException e) {
      // TODO Auto-generated catch block
      System.out.println(e.getMessage());
      new HelpFormatter().printHelp("count", options);
    }
  }

}
