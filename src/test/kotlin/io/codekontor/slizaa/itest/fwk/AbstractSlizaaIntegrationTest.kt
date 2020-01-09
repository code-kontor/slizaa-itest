/**
 * Slizaa Documentation - Slizaa Static Software Analysis Tools
 * Copyright © 2019 Code-Kontor GmbH and others (slizaa@codekontor.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package io.codekontor.slizaa.itest.fwk

import io.codekontor.slizaa.core.boltclient.IBoltClient
import io.codekontor.slizaa.core.boltclient.IBoltClientFactory
import io.codekontor.slizaa.server.SlizaaServerConfiguration
import io.codekontor.slizaa.server.service.backend.IModifiableBackendService
import io.codekontor.slizaa.server.service.backend.extensions.Version
import io.codekontor.slizaa.server.service.backend.extensions.mvn.MvnBasedExtension
import io.codekontor.slizaa.server.service.backend.extensions.mvn.MvnDependency
import io.codekontor.slizaa.server.service.selection.ISelectionService
import io.codekontor.slizaa.server.service.slizaa.IGraphDatabase
import io.codekontor.slizaa.server.service.slizaa.IHierarchicalGraph
import io.codekontor.slizaa.server.service.slizaa.ISlizaaService
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.shell.jline.InteractiveShellApplicationRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

@RunWith(SpringRunner::class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = [
            InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=" + false,
            "server.port=8085"
        ]
)
@ContextConfiguration(classes = [SlizaaServerConfiguration::class])
abstract class AbstractSlizaaIntegrationTest {

    @Autowired
    private lateinit var modifiableBackendService: IModifiableBackendService
        private set

    @Autowired
    private lateinit var slizaaService: ISlizaaService
        private set

    @Autowired
     lateinit var selectionService: ISelectionService
        private set

    @LocalServerPort
    var randomServerPort = 0
        private set

    lateinit var graphDatabase : IGraphDatabase
        private set

    lateinit var hierarchicalGraph : IHierarchicalGraph
        private set

    lateinit var databaseClient : IBoltClient
        private set

    @Before
    @Throws(IOException::class)
    fun prepare() {

        // configure extensions
        if (!modifiableBackendService.hasInstalledExtensions()) {
            modifiableBackendService!!.installExtensions(listOf(MvnBasedExtension("slizaa-extensions-jtype", Version(1, 0, 0))
                    .withDependency(MvnDependency("io.codekontor.slizaa.extensions:slizaa-extensions-jtype:1.0.0-SNAPSHOT"))))
        }

        // configure graph database
        if (!slizaaService!!.hasGraphDatabases()) {
            val graphDatabase = slizaaService.newGraphDatabase("test")
            graphDatabase.setContentDefinitionProvider("directory", TestContentInitializer.exampleContentPath)
            graphDatabase.parse(true)
        }

        graphDatabase = slizaaService.getGraphDatabase("test")

        if (!graphDatabase.isRunning()) {
            graphDatabase.start()
        }

        if (graphDatabase.getHierarchicalGraph("hg01") == null) {
            graphDatabase.newHierarchicalGraph("hg01")
        }

        hierarchicalGraph = graphDatabase.getHierarchicalGraph("hg01")

        databaseClient = hierarchicalGraph.rootNode.getExtension(IBoltClient::class.java)
        Assertions.assertThat(databaseClient).isNotNull()
    }

    companion object {

        @BeforeClass
        @JvmStatic
        @Throws(IOException::class)
        fun configureSlizaaInstance() {
            val workDir = File("slizaa-work")
            System.setProperty("database.rootDirectory", workDir.toString() + File.separator + "databases")
            System.setProperty("configuration.rootDirectory", workDir.toString() + File.separator + "configuration")
            TestContentInitializer.exampleContentPath
        }
    }
}