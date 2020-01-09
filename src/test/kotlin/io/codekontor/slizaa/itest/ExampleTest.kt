package io.codekontor.slizaa.itest

import io.codekontor.slizaa.core.boltclient.IBoltClient
import io.codekontor.slizaa.core.boltclient.IBoltClientFactory.newInstance
import io.codekontor.slizaa.hierarchicalgraph.core.model.HGNode
import io.codekontor.slizaa.itest.fwk.AbstractSlizaaIntegrationTest
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.io.IOException
import java.util.concurrent.Executors


class ExampleTest : AbstractSlizaaIntegrationTest() {

    @Test
    @Throws(IOException::class)
    fun test() {

        //
        val aggregatedDependency = packageNode("org/hibernate/cfg").getOutgoingDependenciesTo(packageNode("org/hibernate/mapping"))
        assertThat(aggregatedDependency).isNotNull()
        assertThat(aggregatedDependency.coreDependencies).hasSize(259)
        aggregatedDependency.coreDependencies.forEach({
            dep  -> println("${dep.from} --> ${dep.to}")
        })

        //
        val typeNpdeBinderHelper = typeNode("org.hibernate.cfg.BinderHelper");

        var referencedTargetNodes = selectionService.getReferencedTargetNodes(
                aggregatedDependency,
                listOf(typeNpdeBinderHelper),
                true)

        println(referencedTargetNodes)

        var nodes = selectionService.getChildrenFilteredByDependencySources(
                aggregatedDependency,
                typeNpdeBinderHelper)

        referencedTargetNodes = selectionService.getReferencedTargetNodes(
                aggregatedDependency,
                listOf(typeNpdeBinderHelper),
                true)

        println(referencedTargetNodes)


        /** val body = "{\"query\": \"query GraphDatabasesWithHierarchicalGraphs { graphDatabases { identifier hierarchicalGraphs { identifier } } }\" }"
        val result = Request.Post("http://localhost:8085/graphql")
                .bodyByteArray(body.toByteArray())
                .connectTimeout(1000)
                .socketTimeout(1000)
                .execute().returnContent().asString()
        println(result) **/

        // org.hibernate.mapping.Column
        // org.hibernate.cfg.BinderHelper
    }

    private fun node(id: Long) : HGNode {
        val result = hierarchicalGraph.rootNode.lookupNode(id);
        assertThat(result).isNotNull()
        return result
    }

    private fun packageNode(fqn : String) : HGNode {
        var queryResult = databaseClient.syncExecCypherQuery("MATCH (p:Package {fqn:'$fqn'}) RETURN id(p) as id")
        val id = queryResult.single().get("id").asLong()
        return node(id)
    }

    private fun typeNode(fqn : String) : HGNode {
        var queryResult = databaseClient.syncExecCypherQuery("MATCH (t:Type {fqn:'$fqn'}) RETURN id(t) as id")
        val id = queryResult.single().get("id").asLong()
        return node(id)
    }
}