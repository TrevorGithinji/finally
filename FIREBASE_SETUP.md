# Firebase Setup Guide

This guide will help you set up Firebase for your Android app to enable user authentication, database storage, and image uploads.

## Step 1: Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" or "Add project"
3. Enter a project name (e.g., "Final Resort")
4. Choose whether to enable Google Analytics (optional)
5. Click "Create project"

## Step 2: Add Android App to Firebase

1. In your Firebase project console, click the Android icon to add an Android app
2. Enter your package name: `com.trevor.final_resort`
3. Enter app nickname (optional): "Final Resort"
4. Click "Register app"

## Step 3: Download Configuration File

1. Download the `google-services.json` file
2. Place it in the `app/` directory of your Android project
3. **Important**: Replace the placeholder `google-services.json` file with the actual one from Firebase

## Step 4: Enable Authentication

1. In Firebase Console, go to "Authentication" in the left sidebar
2. Click "Get started"
3. Go to the "Sign-in method" tab
4. Enable "Email/Password" authentication
5. Click "Save"

## Step 5: Set Up Firestore Database

1. In Firebase Console, go to "Firestore Database" in the left sidebar
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select a location for your database (choose the closest to your users)
5. Click "Done"

## Step 6: Set Up Firebase Storage

1. In Firebase Console, go to "Storage" in the left sidebar
2. Click "Get started"
3. Choose "Start in test mode" (for development)
4. Select a location for your storage (same as database)
5. Click "Done"

## Step 7: Security Rules (Optional but Recommended)

### Firestore Security Rules
Go to Firestore Database > Rules and update with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read all products
    match /products/{productId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Allow users to read/write their own user data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Storage Security Rules
Go to Storage > Rules and update with:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Allow authenticated users to upload product images
    match /products/{productId}/{imageId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## Step 8: Build and Test

1. Sync your project with Gradle files
2. Build the project
3. Run the app on a device or emulator
4. Test user registration and login
5. Test adding products with images

## Troubleshooting

### Common Issues:

1. **"google-services.json not found"**
   - Make sure the file is in the `app/` directory
   - Check that the package name matches your app

2. **Authentication errors**
   - Verify Email/Password authentication is enabled in Firebase Console
   - Check that the user doesn't already exist (for registration)

3. **Database permission errors**
   - Make sure Firestore is created and in test mode
   - Check security rules if you've customized them

4. **Image upload failures**
   - Verify Firebase Storage is set up
   - Check storage security rules
   - Ensure you have internet connectivity

### Testing the Setup:

1. **User Registration**: Try creating a new account
2. **User Login**: Try logging in with the created account
3. **Product Creation**: Add a product with images
4. **Product Listing**: View all products
5. **Product Details**: View individual product details
6. **Rating System**: Add ratings to products

## Next Steps

Once Firebase is working:

1. **Production Setup**: Change security rules from test mode to production
2. **User Management**: Add user profile management features
3. **Notifications**: Implement push notifications
4. **Analytics**: Add Firebase Analytics for user behavior tracking
5. **Crash Reporting**: Enable Firebase Crashlytics for error monitoring

## Support

If you encounter issues:
1. Check the Firebase Console for error logs
2. Verify your internet connection
3. Ensure all dependencies are properly synced
4. Check that the `google-services.json` file is correctly placed and configured 