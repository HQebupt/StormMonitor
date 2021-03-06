package bolts;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import util.FName;
import util.StreamId;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class ProductProCity extends BaseRichBolt {

	private static final long serialVersionUID = 1L;
	OutputCollector collector;
	Map<String, Integer> provinceItem = new HashMap<String, Integer>();
	static Logger log = Logger.getLogger(ProductProCity.class);

	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(Tuple input) {
		try {
			String proCity = input.getStringByField(FName.PROCITY.name());
			String ordertype = input.getStringByField(FName.ORDERTYPE.name());
			String product_id = input.getStringByField(FName.PRODUCT_ID.name());
			int type = 0;
			if (!ordertype.equalsIgnoreCase("")) {
				type = Integer.valueOf(ordertype);
			}
			if ((type == 4) || (type == 5)) {
				String key = proCity + "|" + product_id;
				Integer itemSum = itemCount(key);
				provinceItem.put(key, itemSum);
				collector.emit(StreamId.PROVINCEITEM.name(), new Values(key,
						itemSum));
			}
		} catch (IllegalArgumentException e) {
			if (input.getSourceStreamId().equals(StreamId.SIGNAL24H.name())) {
				log.info("24Hour is coming.");
				provinceItem.clear();
			}
		}
		collector.ack(input);
	}

	private int itemCount(String key) {
		Integer count = getItemCount(key);
		count++;
		return count;
	}

	private int getItemCount(String key) {
		Integer count = provinceItem.get(key);
		if (count == null) {
			count = 0;
		}
		return count;
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream(StreamId.PROVINCEITEM.name(), new Fields(
				FName.PROCITYITEM.name(), FName.COUNT.name()));
	}

	public void cleanup() {
	}
}
