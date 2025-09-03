# HTTPS in Spring Boot (Beginner-friendly, step-by-step)

> ✅ In the sample project, the keystore/truststore files are already included. 
> You can run immediately and/or regenerate them following the steps below.

---

## What are these files?

- Keystore (`server-keystore.p12`): Lives on the server. Holds the private key and 
the certificate that the server presents to clients.
- Certificate (`server-public.cer`): The public certificate extracted from the keystore. You can share this freely.
- Truststore (`client-truststore.p12`): Lives on the client. Tells the client which certificates 
to trust (we add our self-signed cert here).

Because the cert is **self-signed** (not issued by a public CA like Let’s Encrypt), 
clients won’t trust it by default—hence the truststore.

---

## Quick start (use the files already in the project)

### 1. File locations (example)

```shell
src/main/resources/serverkey/server-keystore.p12
src/main/resources/serverkey/client-truststore.p12
src/main/resources/serverkey/server-public.cer
```

### 2. Spring Boot config (`application.properties`)

```properties
# ==== HTTPS settings ====
server.ssl.enabled=${ENABLED_SSL:true}
server.port=${SERVER_PORT:8443}   # 443 requires admin/root; use 8443 for dev

server.ssl.key-store-type=PKCS12
server.ssl.key-store=${SERVER_KEY_STORE_PATH:classpath:serverkey/server-keystore.p12}
server.ssl.key-store-password=${SERVER_KEY_STORE_PASSWORD:passsword}   # <-- matches your keystore
server.ssl.key-alias=server-key
# server.ssl.key-password=passsword  # only needed if key password differs from keystore password
```

### 3. Run the app

```shell
./gradlew bootRun
```

#### 4. Open in browser

Visit: https://localhost:8443
Your browser will warn that the cert is not trusted (it’s self-signed). 
For testing, you can proceed or import `server-public.cer` into your OS/browser trust store.

> ⚠️ Self-signed certs are for development/testing only. Use a CA-signed cert in production.

---

## Regenerate everything yourself (optional)

If you want to recreate the files from scratch, here are the exact commands you shared, 
with brief explanations and non-interactive flags (so you can script them). Replace CN=localhost 
if your server uses another hostname.

```shell
# 1) Create a server keystore with a self-signed cert (valid 10 years)
keytool -genkeypair \
  -alias server-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650 \
  -keystore server-keystore.p12 \
  -storetype PKCS12 \
  -storepass passsword \
  -keypass passsword \
  -dname "CN=localhost, OU=Dev, O=Example, L=City, S=State, C=US"

# 2) Export the public certificate from the keystore
keytool -export \
  -alias server-key \
  -keystore server-keystore.p12 \
  -storepass passsword \
  -file server-public.cer

# 3) Create a client truststore that trusts our server cert
keytool -import \
  -alias server-cert \
  -file server-public.cer \
  -keystore client-truststore.p12 \
  -storetype PKCS12 \
  -storepass passsword \
  -noprompt
```

> Notes
> 
> `-alias`: the name you’ll reference (`server-key` in Spring config).
> 
> `-storepass` / `-keypass`: passwords; here both are password to keep it simple.
> 
> `-dname`: the certificate’s subject. Use `CN=<your host>` (e.g., `CN=api.localtest.me`).
> 
> Keep these files out of version control in real projects.

---

## Point Spring Boot at the keystore

Make sure the keystore file is on your classpath (e.g., `src/main/resources/serverkey/server-keystore.p12`) 
or provide an absolute path:

```properties
server.ssl.key-store=classpath:serverkey/server-keystore.p12
# or
# server.ssl.key-store=/absolute/path/to/server-keystore.p12
```

If you set environment variables, they will override the defaults:
```shell
export ENABLED_SSL=true
export SERVER_PORT=8443
export SERVER_KEY_STORE_PATH=/absolute/path/server-keystore.p12
export SERVER_KEY_STORE_PASSWORD=password
```

---

## Test the server

### 1. With a browser

Go to https://localhost:8443.

Expect a warning (self-signed). For a clean green lock in dev, import `server-public.cer` into your OS/browser as a trusted certificate.

### 2. With curl

curl prefers PEM. Convert the exported certificate (DER) to PEM once:

```shell
openssl x509 -inform DER -in server-public.cer -out server-public.pem
```

Now call your API:

```shell
curl https://localhost:8443/actuator/health --cacert server-public.pem
```

### 3. With Postman

Settings → Certificates → CA Certificates → Import server-public.cer (or .pem).

Or (not recommended) disable “SSL certificate verification” for quick tests.

### 4. Make a Java client trust the server (using the provided truststore)

Option A — JVM system properties (works with most HTTP clients)

```shell
java \
-Djavax.net.ssl.trustStore=/path/to/client-truststore.p12 \
-Djavax.net.ssl.trustStorePassword=passsword \
-Djavax.net.ssl.trustStoreType=PKCS12 \
-jar your-client.jar
```

Option B — Programmatic (Spring RestTemplate)

```shell
KeyStore ts = KeyStore.getInstance("PKCS12");
try (var in = Files.newInputStream(Path.of("client-truststore.p12"))) {
ts.load(in, "password".toCharArray());
}

TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
tmf.init(ts);

SSLContext ctx = SSLContext.getInstance("TLS");
ctx.init(null, tmf.getTrustManagers(), new SecureRandom());

HttpClient httpClient = HttpClientBuilder.create()
.setSSLContext(ctx)
.build();

ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
RestTemplate restTemplate = new RestTemplate(requestFactory);

// Test call
String body = restTemplate.getForObject("https://localhost:8443/health", String.class);
System.out.println(body);
```

(For **OkHttp**, **Apache HttpClient 5**, **WebClient**, etc., the idea is the same: load the truststore and build an SSL context.)

---

## Common pitfalls & fixes

- **“Keystore was tampered with, or password was incorrect”**
Your `server.ssl.key-store-password` doesn’t match the keystore’s password (`password` in this guide).
- **“Alias name not found”**
Ensure `server.ssl.key-alias=server-key` (and that the keystore actually contains that alias). Check with:

```shell
keytool -list -v -keystore server-keystore.p12 -storepass password
```

- **Binding to port 443 fails**  
On Linux/macOS, ports <1024 need admin privileges. Use `server.port=8443` for development.
- Browser still warns after importing  
Ensure the cert’s CN (or Subject Alternative Name) matches the hostname you’re visiting (e.g., localhost).

---

## Production notes (short & sweet)  
- Don’t use self-signed certs in production.
- Use a CA-issued cert (e.g., Let’s Encrypt) or terminate TLS at a reverse proxy (Nginx/Traefik) and forward to your app.
- Rotate and secure your keystores, and never commit real secrets to git.

---

## TL;DR (Text Long; Didn't Read)

1. Keystore (server) → contains private key + cert.
2. Export cert → shareable public cert.
3. Truststore (client) → add that cert so your client trusts the server. 
4. Spring Boot points to `server-keystore.p12`, with the right password (`password`) and alias (`server-key`). 
5. Run at `https://localhost:8443`, test with browser/curl/Postman.
