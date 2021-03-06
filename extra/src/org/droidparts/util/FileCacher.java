/**
 * Copyright 2012 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.util;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.graphics.drawable.BitmapDrawable;

//TODO make universal

public class FileCacher {

	private final File cacheDir;

	public FileCacher(File cacheDir) {
		this.cacheDir = cacheDir;
		cacheDir.mkdirs();
	}

	public boolean saveToCache(String name, BitmapDrawable drawable) {
		File file = new File(cacheDir, getFileName(name));
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file),
					BUFFER_SIZE);
			drawable.getBitmap().compress(PNG, 100, bos);
			return true;
		} catch (Exception e) {
			L.e(e);
			return false;
		} finally {
			silentlyClose(bos);
		}
	}

	public BitmapDrawable readFromCache(String name) {
		File file = new File(cacheDir, getFileName(name));
		if (file.exists()) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file),
						BUFFER_SIZE);
				BitmapDrawable drawable = new BitmapDrawable(bis);
				// only after successful restore
				file.setLastModified(System.currentTimeMillis());
				return drawable;
			} catch (Exception e) {
				L.e(e);
				return null;
			} finally {
				silentlyClose(bis);
			}
		} else {
			L.i("No image file: " + file.getAbsolutePath());
			return null;
		}
	}

	public void purgeCache(long lastAccessedBefore) {
		for (File f : IOUtils.getFileList(cacheDir, null)) {
			if (lastAccessedBefore <= 0
					|| f.lastModified() < lastAccessedBefore) {
				f.delete();
			}
		}
	}

	private static String getFileName(String name) {
		return HashCalc.getMD5(name);
	}

}
