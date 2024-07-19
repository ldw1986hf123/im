//package com.kuailu.im.server.es;
//
//import com.kuailu.im.server.model.entity.Item;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
//import org.springframework.data.elasticsearch.core.query.IndexQuery;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class IndexTest {
//
//    @Autowired
//    private ElasticsearchTemplate elasticsearchTemplate;
//
//    @Test
//    public void testCreate(){
//        long start = System.currentTimeMillis();
//        int counter = 0;
//        try {
//            List<IndexQuery> queries = new ArrayList<>();
//            for (SysLogs log : logList) {
//                IndexQuery indexQuery = new IndexQuery();
//                indexQuery.setId(log.getId()+ "");
//                indexQuery.setObject(log);
//                indexQuery.setIndexName("elasticsearch");
//                indexQuery.setType("sysLog");
//                //也可以使用IndexQueryBuilder来构建
//                //IndexQuery index = new IndexQueryBuilder().withId(person.getId() + "").withObject(person).build();
//                queries.add(indexQuery);
//                if (counter % 1000 == 0) {
//                    elasticsearchTemplate.bulkIndex(queries);
//                    queries.clear();
//                    System.out.println("bulkIndex counter : " + counter);
//                }
//                counter++;
//            }
//            if (queries.size() > 0) {
//                elasticsearchTemplate.bulkIndex(queries);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println("bulkIndex completed use time:"+ (end-start));
//
//        } catch (Exception e) {
//            System.out.println("IndexerService.bulkIndex e;" + e.getMessage());
//            throw e;
//        }
//    }
//}