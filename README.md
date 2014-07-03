redikka
=======

Redikka is a pet project that uses Akka features to implement a Redis complient server, that should work with any redis client.

Todo
=====
- [x] Basic Skelton (GET command, In Memory)
- [ ] Basic Command (SET command, not all of them).
- [ ] Being Cluster resizing aware ([By doing handovers](https://groups.google.com/forum/#!msg/akka-user/aIBAnHex5Wg/nIz41GEpAPUJ), or using (ClusterSingletonManager)[http://doc.akka.io/api/akka/2.1.1/index.html#akka.contrib.pattern.ClusterSingletonManager])
- [ ] Akka Persistance 
- [ ] Rest of Commands
