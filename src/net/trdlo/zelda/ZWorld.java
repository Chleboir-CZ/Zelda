package net.trdlo.zelda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.trdlo.zelda.exceptions.ZException;

public abstract class ZWorld {

	public abstract void update();
	
	public final void saveToFile(File file, boolean compress) throws ZException {
		if (compress) {
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))))) {
				saveToWriter(writer);
			} catch (FileNotFoundException ex) {
				throw new ZException("Could not save world: file not found", ex);
			} catch (IOException ex) {
				throw new ZException("Could not save world: I/O exception", ex);
			}
		} else {
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
				saveToWriter(writer);
			} catch (FileNotFoundException ex) {
				throw new ZException("Could not save world: file not found", ex);
			} catch (IOException ex) {
				throw new ZException("Could not save world: I/O exception", ex);
			}
		}
	}

	protected abstract void saveToWriter(BufferedWriter writer) throws ZException;
	
	protected static BufferedReader getReader(File file, boolean compress) throws ZException {
		BufferedReader reader;
		if (compress) {
			try {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			} catch (FileNotFoundException ex) {
				throw new ZException("Could not load world: file not found", ex);
			} catch (IOException ex) {
				throw new ZException("Could not load world: I/O exception", ex);
			}
		} else {
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			} catch (FileNotFoundException ex) {
				throw new ZException("Could not load world: file not found", ex);
			}
		}
		return reader;
	}
}
