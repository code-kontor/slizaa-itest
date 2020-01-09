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

import io.codekontor.mvnresolver.MvnResolverServiceFactoryFactory
import org.apache.commons.io.FileUtils
import java.io.File

object TestContentInitializer  {

    private const val EXAMPLE_DIRECTORY = "slizaa-content"

    private val CONTENT_LIST = arrayOf("org.hibernate:hibernate-core:5.4.10.Final")

    private var exampleContentDirectory: File

    val exampleContentPath: String
        get() = exampleContentDirectory!!.absolutePath

    init {
        // setup the mvm resolver service
        val mvnResolverService = MvnResolverServiceFactoryFactory
                .createNewResolverServiceFactory().newMvnResolverService().withMavenCentralRepo().create()

        // create the example directory
        exampleContentDirectory = File(EXAMPLE_DIRECTORY)

        // populate example content directory
        if (exampleContentDirectory!!.exists()) {
            FileUtils.deleteDirectory(exampleContentDirectory)
        } else {
            exampleContentDirectory!!.mkdirs()
        }

        // copy the files to the example directory
        val resolvedFiles = mvnResolverService.resolve(false, *CONTENT_LIST)
        for (resolvedFile in resolvedFiles) {
            FileUtils.copyFileToDirectory(resolvedFile, exampleContentDirectory)
        }
    }
}