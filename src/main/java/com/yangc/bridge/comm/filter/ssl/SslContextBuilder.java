package com.yangc.bridge.comm.filter.ssl;

import java.io.File;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;

import com.yangc.utils.Message;

public class SslContextBuilder {

	private static final Logger logger = Logger.getLogger(SslContextBuilder.class);

	private static final String KEYSTORE_PASSWORD = Message.getMessage("bridge.keystore");
	private static final String TRUSTSTORE_PASSWORD = Message.getMessage("bridge.truststore");

	public SSLContext build() {
		SSLContext sslContext = null;

		String path = SslContextBuilder.class.getResource("//").getFile();
		File keyStoreFile = new File(path + "server_keystore.jks"), trustStoreFile = new File(path + "server_truststore.jks");
		if (keyStoreFile.exists() && trustStoreFile.exists()) {
			logger.info("SslContextBuilder - keystore=" + keyStoreFile.getAbsolutePath() + ", truststore=" + trustStoreFile.getAbsolutePath());
			try {
				KeyStoreFactory keyStoreFactory = new KeyStoreFactory();
				keyStoreFactory.setDataFile(keyStoreFile);
				keyStoreFactory.setPassword(KEYSTORE_PASSWORD);

				KeyStoreFactory trustStoreFactory = new KeyStoreFactory();
				trustStoreFactory.setDataFile(trustStoreFile);
				trustStoreFactory.setPassword(TRUSTSTORE_PASSWORD);

				SslContextFactory sslContextFactory = new SslContextFactory();
				sslContextFactory.setKeyManagerFactoryKeyStore(keyStoreFactory.newInstance());
				sslContextFactory.setKeyManagerFactoryKeyStorePassword(KEYSTORE_PASSWORD);
				sslContextFactory.setTrustManagerFactoryKeyStore(trustStoreFactory.newInstance());
				sslContext = sslContextFactory.newInstance();
				logger.info("SslContextBuilder - protocol=" + sslContext.getProtocol() + ", provider=" + sslContext.getProvider());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logger.error("keystore or truststore file does not exist");
		}
		return sslContext;
	}

}
