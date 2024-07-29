//package com.kuailu.im.server.es;
//
//import com.google.common.collect.Lists;
//import com.kuailu.im.server.model.entity.Employee;
//import com.kuailu.im.server.starter.BaseJunitTest;
//import org.junit.Test;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
//import org.springframework.data.elasticsearch.core.query.Criteria;
//import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
//import org.springframework.data.elasticsearch.core.query.IndexQuery;
//import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 增 删 改 测试
// *
// * @author liuqiuyi
// * @date 2021/4/28 20:47
// */
//public class CrudTest extends BaseJunitTest {
//    @Resource
//    ElasticsearchOperations elasticsearchOperations;
//
//    /**
//     * 单个保存 or 更新
//     * <p>
//     * id 不存在,保存
//     * id 已经存在,更新
//     *
//     * @author liuqiuyi
//     * @date 2021/4/28 20:48
//     */
//    @Test
//    public void saveOrUpdateTest() {
//        Employee employee = new Employee(17L, "liuqiuyi", "全栈", 25, "男");
//
//        IndexQuery indexQuery = new IndexQueryBuilder()
//                .withObject(employee)
//                .build();
//        String index = elasticsearchOperations.index(indexQuery, IndexCoordinates.of("person"));
//        // 返回的 index 是数据 id,如果指定了,返回指定的 id 值,未指定,返回一个 es 自动生成的
//        System.out.println(index);
//    }
//
//
//    /**
//     * 批量保存
//     *
//     * @author liuqiuyi
//     * @date 2021/4/29 19:53
//     */
//    @Test
//    public void batchSaveTest() {
//        Employee employeeA = new Employee(18L, "liuqiuyi", "java", 25, "男");
//        Employee employeeB = new Employee(19L, "liuqiuyi", "java", 25, "男");
//        ArrayList<Employee> employeeArrayList = Lists.newArrayList(employeeA, employeeB);
//
//        List<IndexQuery> indexQueryList = Lists.newArrayList();
//        for (Employee employee : employeeArrayList) {
//            IndexQuery indexQuery = new IndexQueryBuilder()
//                    .withObject(employee)
//                    .build();
//
//            indexQueryList.add(indexQuery);
//        }
//
//        elasticsearchOperations.bulkIndex(indexQueryList, Employee.class);
//    }
//
//    /**
//     * 根据 id 单个删除
//     *
//     * @author liuqiuyi
//     * @date 2021/4/29 20:30
//     */
//    @Test
//    public void deleteByIdTest() {
//        String delete = elasticsearchOperations.delete(Employee.class);
//        System.out.println(delete);
//    }
//
//    /**
//     * 批量删除
//     *
//     * @author liuqiuyi
//     * @date 2021/5/6 15:37
//     */
//    @Test
//    public void batchDeleteByIdsTest() {
//        CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria());
//        criteriaQuery.setIds(Lists.newArrayList("18", "19"));
//
//        elasticsearchOperations.delete(criteriaQuery, Employee.class);
//    }
//}