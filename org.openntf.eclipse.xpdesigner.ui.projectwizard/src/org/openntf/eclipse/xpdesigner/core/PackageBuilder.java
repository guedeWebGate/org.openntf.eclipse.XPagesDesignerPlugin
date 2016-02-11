package org.openntf.eclipse.xpdesigner.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public enum PackageBuilder {
	INSTANCE;

	public void buildPackage(IProject project, String targetFilename) throws CoreException {
		// Create a buffer for reading the files

		IResource[] files = project.members();

		try {
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFilename));

			// Compress the files
			processResources2ZIPStream(files, out, "");

			// Complete the ZIP file
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processResources2ZIPStream(IResource[] files, ZipOutputStream out, String path)
			throws FileNotFoundException, IOException, CoreException {
		byte[] buf = new byte[1024];

		for (IResource resource : files) {
			if (resource instanceof IFile) {
				System.out.println("process " +path + resource.getName() + " / " + resource.getLocation().toOSString());
				File fileToAppend = resource.getLocation().toFile();
				if (fileToAppend.canRead()) {
					FileInputStream in = new FileInputStream(resource.getLocation().toFile());

					// Add ZIP entry to output stream.
					out.putNextEntry(new ZipEntry(path + resource.getName()));

					// Transfer bytes from the file to the ZIP file
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}

					// Complete the entry
					out.closeEntry();
					in.close();
				}
			}
			if (resource instanceof IFolder) {
				String subPath = path + resource.getName() + "/";
				IResource[] subFiles = ((IFolder) resource).members();
				processResources2ZIPStream(subFiles, out, subPath);
			}
		}
	}

}
