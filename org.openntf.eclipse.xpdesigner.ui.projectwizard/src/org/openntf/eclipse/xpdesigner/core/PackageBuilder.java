package org.openntf.eclipse.xpdesigner.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public enum PackageBuilder {
	INSTANCE;
	
	public void buildPackage(IProject project, String targetFilename) throws CoreException {
    // Create a buffer for reading the files
    byte[] buf = new byte[1024];

    IResource[] files = project.members();
    
    try {
        // Create the ZIP file
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFilename));

        // Compress the files
        for (int i=0; i<files.length; i++) {
        	IResource resource = files[i];
        	System.out.println("process "+resource.getName() +" / "+ resource.getFullPath().toOSString());
            FileInputStream in = new FileInputStream(resource.getFullPath().toFile());

            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(resource.getName()));

            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Complete the entry
            out.closeEntry();
            in.close();
        }

        // Complete the ZIP file
        out.close();
    } catch (IOException e) {
    }
	}

}
