/*
 *      Copyright 2014 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package org.schedulesdirect.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Helper methods for EpgClient; handles common functionality
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
final class EpgClientHelper {
	static public void writeLogoToFile(final InputStream ins, final File dest) throws IOException {
		boolean destExists = dest.exists();
		try(FileOutputStream outs = new FileOutputStream(dest)) {
			if(ins != null)
				IOUtils.copy(ins, outs);
			else if(!destExists)
				dest.delete();
		} catch(IOException e) {
			dest.delete();
			throw e;
		}
	}
	
	private EpgClientHelper() {}
}
