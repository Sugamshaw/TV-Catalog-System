<h1>📺 TV Catalog App</h1>

<p>A dual-application system built for <strong>wholesalers</strong> to manage their product catalog via a mobile app and <strong>display real-time content on Android TV</strong>, enhancing in-store or booth customer engagement.</p>

<hr>

<h2>🛠️ Features</h2>

<h3>📱 Mobile App (Android)</h3>
<ul>
  <li>Built using <strong>Kotlin</strong> in <strong>Android Studio</strong>.</li>
  <li>Add, edit, and delete product entries.</li>
  <li>Upload product images and details.</li>
  <li>Login/signup with secure Firebase <strong>Authentication</strong>.</li>
  <li>Real-time sync of product data via <strong>Firebase Firestore</strong>.</li>
  <li>Product images are stored and accessed from <strong>Firebase Storage</strong>.</li>
</ul>

<h3>📺 TV App (Android TV)</h3>
<ul>
  <li>Developed for Android TV screens.</li>
  <li>Displays updated product catalog in real-time.</li>
  <li>Automatically fetches changes from Firebase Firestore.</li>
  <li>Clean, auto-scrolling product layout for easy customer viewing.</li>
</ul>

<hr>

<h2>⚙️ Tech Stack</h2>

<table>
  <tr><th>Component</th><th>Technology</th></tr>
  <tr><td>Mobile App</td><td>Android (Kotlin)</td></tr>
  <tr><td>TV Display App</td><td>Android TV (Kotlin)</td></tr>
  <tr><td>Backend</td><td>Firebase Firestore, Storage, Authentication</td></tr>
  <tr><td>IDE</td><td>Android Studio</td></tr>
</table>

<hr>

<h2>🚀 Setup Instructions</h2>

<h3>Prerequisites</h3>
<ul>
  <li>Android Studio installed with Android SDK and Kotlin.</li>
  <li>A Firebase project with Firestore, Storage, and Authentication enabled.</li>
</ul>

<h3>1. Clone the Repositories</h3>
<pre><code class="language-bash">
git clone https://github.com/Sugamshaw/TV-Catalog-System
</code></pre>

<h3>2. Firebase Configuration</h3>
<ul>
  <li>Download <code>google-services.json</code> from your Firebase Console.</li>
  <li>Place it inside each app’s <code>app/</code> directory.</li>
  <li>Make sure Firestore and Storage security rules are configured properly.</li>
</ul>

<h3>3. Build & Run</h3>

<h4>📱 Mobile App</h4>
<pre><code class="language-bash">
# Open in Android Studio
# Connect Android phone or emulator
# Build & run the app
</code></pre>

<h4>📺 TV App</h4>
<pre><code class="language-bash">
# Open in Android Studio
# Connect to Android TV or emulator
# Build & deploy the app
</code></pre>


<hr>

<h2>📌 Use Cases</h2>
<ul>
  <li>Retail shops with in-store Android TVs.</li>
  <li>Wholesale product booths and exhibitions.</li>
  <li>Realtime, hands-free product promotion in physical locations.</li>
</ul>

<hr>

<h2>🔐 Security</h2>
<ul>
  <li>Firebase Authentication protects access to data.</li>
  <li>Firestore and Storage security rules ensure user data isolation.</li>
  <li>No sensitive data stored on-device.</li>
</ul>

<hr>

