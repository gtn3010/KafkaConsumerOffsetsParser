package com.monitoring.kafka;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.group.GroupMetadataManager;
import kafka.coordinator.group.OffsetKey;
import scala.Option;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.log4j.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Properties;

public class KafkaConsumerOffsetsParser {

    static String KAFKA_BOOTSTRAP_SERVERS = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
    static String OFFSET_TOPIC = System.getenv("OFFSET_TOPIC_NAME");
    static String KAFKA_USER = System.getenv("KAFKA_USER");
    static String KAFKA_PASSWORD = System.getenv("KAFKA_PASSWORD");

	private static KafkaConsumer<byte[], byte[]> consumer;
	private static final Logger LOGGER = Logger.getLogger(KafkaConsumerOffsetsParser.class);

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "offset-reader");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, "Kafka_ConsumerOffsets_Monitor");
		props.put("exclude.internal.topics", "false");
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + KAFKA_USER + "\" password=\"" + KAFKA_PASSWORD + "\";");

		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(OFFSET_TOPIC));

		while (true) {
			try {
				ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(100);
				if (consumerRecords.count() > 0) {
					consumerRecords.forEach(consumerRecord -> {
						byte[] key = consumerRecord.key();
						byte[] value;
						if (key != null) {
							Object o = GroupMetadataManager.readMessageKey(ByteBuffer.wrap(key));
							if (o != null && o instanceof OffsetKey) {
								OffsetKey offsetKey = (OffsetKey) o;
								value = consumerRecord.value();
								OffsetAndMetadata offsetAndMetadata = GroupMetadataManager
										.readOffsetMessageValue(ByteBuffer.wrap(value));
								// For print purpose
								Object groupTopicPartition = offsetKey.key();
								String formattedValue = String
										.valueOf(GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(value)));
								// For print purpose

								ConsumerOffsetDetails detail = new ConsumerOffsetDetails();
								detail.setTopic(offsetKey.key().topicPartition().topic());
								detail.setVersion(offsetKey.version());
								detail.setPartition(offsetKey.key().topicPartition().partition());
								detail.setOffset(offsetAndMetadata.offset());
								detail.setMetadata(offsetAndMetadata.metadata());
								detail.setGroup(offsetKey.key().group());
								detail.setExpireTimestamp(offsetAndMetadata.expireTimestamp().getOrElse(() -> 0L));
								detail.setCommitTimestamp(offsetAndMetadata.commitTimestamp());

								ObjectMapper om = new ObjectMapper();
								try {
									LOGGER.info(om.writeValueAsString(detail));
								} catch (JsonProcessingException e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			consumer.commitSync();
		}

	}
}
