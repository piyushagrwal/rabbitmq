	  
**RabbitMQ**

**Rabbit MQ Queue Types:**

1. **Classic Queue:** Default queue type. Good performance, High availability and data replication are not critical, single node for message storage, rely on disk and memory for persistence, message loss can occur if primary node fails, used where some message loss is acceptable.

Good Match in a simple environment with one rabbitmq mode.   
Can be lazy or not  
For clusters of queues like in production environment use quorum queue.

2. **Quorum Queue:** Designed to prevent data loss in case of node failure. Replicate data across multiple servers. Ensure data remains consistent. Suitable where data integrity and no message loss  is critical. Has cluster of odd nodes (3,5,7…)

Always Lazy(data stored in disk)

3. **Stream**: Dedicated section layer, behaviour different from queue.

First create a rabbitmq queue in rabbitmq interface.

Use RabbitTemplate in spring boot for sending messages. It takes a queue name, converts a message to a byte array and sends it to the default exchange.  
Spring RabbitMQ publishes messages as persistent. Message restored one server restart if queue is durable.  
User rabbitlistener in the consumer application.

In the Queue interface of rabbitmq, purge messages to delete all messages from the queue.

For a scenario, let’s assume our producer produces a message every 500ms and the consumer needs 2000ms to process. Then it will be a bottleneck so we need multiple consumers.  
Spring handles multiple consumers on its own.  
Ideal : fast producer and fast consumer  
Consumer has generally more tasks so it can be slow. So we need to use multiple consumers.  
concurrency in rabbit listener concurrency \= 3-7 (min-max)

JSON for data interchange.  
Java user Jackson or GSON so our application can focus on business logic.  
In Configuration class, create a bean of ObjectMapper and then use it to create json string of the object and then send in message.

Exchanges distribute message to queue based on routing key and copy messages if required. Binding queue to exchanges.

1. Fanout : Broadcast message to multiple queues for a single message.  
2. Direct: Send to selective queues based on routing key. Message discarded if queue not found.  
3. Topic: Used for multiple criteria routing. Based on special characters in routing key.

\* character \- substitute exactly one word  
\# character \- substitute 0 or more words  
Eg. \*.jpg.\* \- Message to all queues with this format of routing key in exchange binding.

**Dead Letter Exchange:**  
Spring by default requeues the message if any exception occurs in consumer class. So it is an infinite exchange loop. We can send a message to Dead Letter Exchange(DLX) with requeue \= false. Other consumers can process it with proper error handling. Also works for timeout.  
After TTL, a timeout occurs and the message is dead.   
Queue can be configured to send dead messages to DLX. While creating a queue, add argument x-dead-letter-exchange.  
A special routing key for the dead letter exchange can be configured while creating a queue.  
To not requeue,   
Solution 1: Automatic \- throw new AmqpRejectAndDontRequeueException("exception text request not queued");   
Solution 2: Manual \- In application.yml, add listener.simple.acknowledge-mode: manual. Then user Channel to reject and acknowledge.  
Spring handles automatically but if manual we need to handle every message.

TTL: When no consumer is available. Add as argument while creating queue. If no consumer takes a message before TTL, it is thrown to dead letter exchange.

**Header Exchange:**  
Add headers to message and send.  
Criteria \- all(AND) and any(OR) in exchange queues while binding add arguments.

**Retry Mechanism**:  
User TTL and DLX to achieve this. Create exchanges. For work, q.work. Then if fail, q.wait then q.dead.  
Consumer must do following- 

* Check retry limit  
* Retry \< threshold, send to wait exchange  
* Else send to dead exchange

Use manual acknowledge in application.yml. Check for x-death header. Get failed retry count from xdeath object.  
DlxProcessingHandlerError checks errors and handler function handles error. User in consumer in exception block  
channel.basicPublish

Use RabbitMQ API, slow but can be used where less knowledge about RabbitMQ. Prefer native.  
Use REST APIs to publish message, consume etc.  
Create user in RabbitMQ Admin, the access can be provided for different exchanges to do the operations

Create a scheduler to sweep dirty queues from time to time.  
Use RabbitmqClient API to clean dirty queues.

To extend functionalities, we can add plugins into rabbitmq. To use, we need to install and then enable the plugin. 

Delay publishing of message is not natively supported so we need to user plugin. Hold message until specified time. To use it,

* Install plugin Delayed Message  
* User exchange type x-delayed-message  
* Add header indicating delay duration (x-delay: 5000L in ms) in messageproperties in producer.

Spring already provides a retry mechanism so we don’t need to create a wait exchange for failure.  
We can configure application.yml for retries.

In spring, SimpleMessageConverter converts string format message to format which rabbitmq understands.

For getting all listeners for rabbitmq, RabbitListenerEndpointregistry registry  
registry.getListenerContainers()  
To Schedule, @Scheduled(cron \= “0 0 23 \* \* \*”)

Unacknowledged messages stay in consumer memory since consumer processes messages one by one. Rabbitmq sends multiple messages to consumer. The number of unacknowledged messages is called prefetch count. Spring has a limit of 250\. We should add consumers if rabbitmq has more messages.  
If slow process \- 1 prefetch count  
If fast process \- High prefetch count  
To set prefetch count in spring, modify application.yml  
We can set multiple prefetch values for multiple consumers. For this, we need to create RabbitListenerContainerFactory instance bean in rabbitmq config class ad then in consumer user containerfactory in @RabbitListener

If message ordering is required, then we should not use multiple consumers for one queue since processing time of each can differ.  
We can use one queue for multiple types of messages. In this way we need to handle less queues.  
Header \_TypeId\_ has different types if the objects set through messages are different.   
In consumer, use rabbitlistener on class so it handles based on headers and then on method, add RabbitHandler annotation and use the class of object sent through message in the parameter.  
For multiple types, we can also create a default handler method.

We can achieve **message ordering** by consistent hash exchange. Rabbitmq has a plugin for this.  
We can add an identifier as a routing key so that the message is published to that queue only.   
**Note**: While using this, before adding any queue,check if there are no pending messages.  
To use:

* Enable consistent hash exchange plugin  
* Create exchange and queue  
* In binding queue to exchange, in routing key, add the number(multiplier). Eg. if you want to send twice messages to then add 2\.  
* In the producer, add routing key number(any since it will be hashed) in method convertand send.

Else can use a single active consumer and if it is down, other takes over. Same as Kafka broker and partition. For this while creating queue, add argument x-single-active-consumer \= true

To check if message is published properly, add config in application.yml. Then postconstruct of rabbittemplate, create a callback method in producer.

If we want to send result to another queue in consumer, we can user rabbithandler and @SendTo(exchange\_name/routing\_key) over the function and return type of function should be the response to be sent to exchange. In this way, consumer can act as producer. (Saga Pattern)

If a message is sent or received from a queue which does not exist, it gives an exception.

Exchanges and queues can also be created from the code itself. For this, create a bean in config class for exchange, queue and binding or it can be done using bean Declarables and in one method all are created.  
To create from consumer class, use @RabbitListener(....) on top of it with parameters bindings and then adding queue and exchange into it.

**Kafka vs Rabbitmq:**

1. In kafka message retention is based on policy(based on number of configured days) and in rabbitmq, by acknowledge.  
2. Kafka doesn’t have a routing mechanism(publishes based on topic and partition) while Rabbitmq has routing key.  
3. Kafka achieves multiple consumers by partitioning (each topic has multiple partitions) but rabbitmq has multiple consumers per queue not guaranteed order.  
4. In kafka consumer pulls messages from topic, rabbitmq pushes messages to consumer  
5. Kafka more scalable for big data.

**Stream**:  
Similar task to queue. Different on how messages are stored and consumed. New message appended to end of stream. Non-destructive consumer semantic.  
In stream, multiple consumers can read the same messages from the same stream(message not removed after consumed). We can configure TTL for messages in the queue. Performance throughput better than queues. Can store large volumes of data with minimal in-memory overhead.  
Always lazy. Requires per-consumer prefetch. No DLX.  
Need to enable rabbitmq stream plugin.  
To create, do the following

* Open port 5552 in docker container.  
* Set advertised\_host  
* Delete and re-create container  
* Create queue of type stream  
* Add rabbit- stream plugin in maven dependencies  
* In the config class, create a bean of RabbitStreamTemplate and send message from producer using that.

Offset in a stream is an index. A stream reads messages from a given offset specification. It can have many specification on basis where should consumer start reading from. 

1. First: consumer starts from first message in stream  
2. Next: starts when publisher publishes next message (default)  
3. Absolute: Start from absolute offset  
4. Last: No offset, starts from beginning  
5. Timestamp: Start from a timestamp (T-5 \-\> data in stream older than 5 mins)

A consumer can start reading from a timestamp also.   
Offset stored in rabbitmq server as part of the stream itself as additional messages. Each consumer can have a different offset. Each consumer must have a unique name. If multiple instances of the same consumer, then their actions are mixed.  
To use different offset types, create an object of the container factory. And add in RabbitListener.  
factory.setConsumerCustomizer().  
We can also set a manual tracking strategy for offset. For manual we must trigger offset in consumer using Context.storeOffset() else it does not store and processes all. Avoid storing frequently as it slows down. Store after few thousand messages. Make consumer processes idempotent.

If we want to avoid stream processing the duplicates for multiple consumers, we should add single active consumer. But it also have chances of duplication at time of switching the consumer. So make the processes idempotent.

**Super Stream:** We can use super stream to process data in parallel multiple streams. Then multiple consumers can read it so we can parallel process data as in kafka topic and partition.   
Creating super streams mean create multiple streams and then bind to exchange using arguments.  
Using message id as routing key in rabbitstreamtemplate has many advantages  
RabbitMQ client library converts message id to routing key. We can use various data types for message id. Messages distributed evenly. Rabbitmq tries best. Same message id goes to the same partition. 

Cannot use RabbitListener for consumer. Environment.maxConsumerByConnection \= 1  
We can also send json to stream by converting Jackson2jsonmessageconverter. 

