package cn.ac.ict.zhangqianxi;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class PutTest {
  private String tableName;
  private String family = "F";
  private String qualifier1="c1";
  private String qualifier2 = "c2";
  private int Range = Integer.MAX_VALUE;
  private int number = 10000;
  private Random random = new Random(7);

  public void initial(String tableName, int Range, int number) {
    this.tableName = tableName;
    this.Range = Range;
    this.number = number;
  }

  public byte[] madeKey(int i) {
    return Bytes.toBytes(String.format("%032d", (long) (i)));
  }

  public void process() throws IOException {
    Configuration conf = HBaseConfiguration.create();
    HTable table=null;
    int count = 0;
    try{
      table=new HTable(conf,tableName);
      table.setAutoFlush(false);
      for (int i = 0; i < number; i++) {
        Put put = new Put(madeKey(random.nextInt(Range)));
        put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier1),
            Bytes.toBytes(i));
        put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier2),
            Bytes.toBytes(i));
        table.put(put);
        count++;
        if (count % 100 == 0)
          System.out.println("count: " + count);
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (table != null)
        table.close();
    }
  }

  public void commandParser(String[] args) {

    String tableName = null;
    int Range = 0;
    int number = 0;
    Options options = new Options();
    options.addOption("t", "tablename", true, "table name");
    options.addOption("r", "Range", true, "range");
    options.addOption("n", "number", true, "number");

    try {
      CommandLine cli = new GnuParser().parse(options, args);
      if (cli.hasOption("t")) {
        tableName = cli.getOptionValue("t");
      } else {
        throw new ParseException(
            "option: -t, --the name of the table which you want to put !");
      }
      if (cli.hasOption("r")) {
        Range = Integer.valueOf(cli.getOptionValue("r"));
      }
      if (cli.hasOption("n")) {
        number = Integer.valueOf(cli.getOptionValue("n"));
      }
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      System.exit(0);
    }
    initial(tableName, Range, number);
  }

  public static void main(String[] args) throws IOException {
    PutTest putTest = new PutTest();
    putTest.commandParser(args);
    putTest.process();
  }
}
