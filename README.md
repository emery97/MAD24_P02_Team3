# MAD24_P02_Team3
## Introduction 
Ticket Finder is a dedicated platform for accessing event tickets in Singapore across various categories, designed to simplify the ticketing process. With its user-friendly interface and comprehensive event database, Ticket Finder aims to enhance the experience of exploring and attending events in Singapore.

## Objective 
To allow people in Singapore to be able to purchase event tickets easily.

## App Category 
Entertainment 

## Team members and student IDs
1. S10258645 Lee Wei Ying
2. S10262850 Ng Joe Yi
3. S10262491 Koh Yung Chun Ivan
4. S10262604 Koh Ye Chyang 
5. S10260078 Tan Si Huei Chloe 

# Ticket Finder App Stage 1 Features

## Login/Register Page [Koh Yung Chun Ivan]
- **Login/Register Fragment**:
  - Provides a fragment that allows users to choose to login or register.
  - Offers a seamless transition between login and registration forms.

- **Firebase Integration**:
  - Account information is checked through Firebase.
  - Newly created accounts are updated in Firebase for secure management using Firebase Authentication.
  - Ensures user data is managed safely and efficiently, leveraging Firebase's robust authentication mechanisms.

- **Forget Password Option**:
  - Available in the login fragment.
  - Prompts a dialogue box asking users for their email.
  - Users can press the cross to exit the dialogue.
    
## Home Page [Ng Joe Yi]
- **Consistent Header**:
  - The header remains visible and consistent across all pages.
    
- **Side Scroll View for "Upcoming Events"**: 
  - Displays a horizontal scrolling list of upcoming events.
  - The top 3 events are displayed based on their upcoming dates.
  - Event information is dynamically extracted from Firebase and populated on the interface.

- **"Recommended for You" Section**:
  - Shows a recycler view of events recommended for the user.
  - Recommendations allows user to view the available list of events that are happening in singapore.

- **Dynamic Event Picture**:
  - The featured event picture displayed below the header changes randomly.
  - Each time the user refreshes the page, a new image is shown.

- **User-Friendly Footer**:
  - Contains navigation icons for easier movement through the app.
  - Designed to be intuitive and enhance the user experience.

## Event Details Page [Tan Si Huei Chloe]

- **Dynamic Event Information [Tan Si Huei Chloe]**:
  - Event details are populated from data in Firebase.
  - Includes:
    - **Artist**: Name of the performing artist or group.
    - **Genre**: Type of music or performance.
    - **Date and Time**: Scheduled date and time of the event.
    - **Venue**: Location of the event.
    - **Description**: Detailed information about the event.
    - **Ticket Price**: Cost of attending the event.
    - **General Sales**: Date and time when tickets go on sale.

- **View Seat Map**:
  - A dialogue allows users to see the concert location map.
  - Includes detailed seating arrangements and ticket pricing.

- **Buy Tickets Button**:
  - Directs users to the ticket purchasing page.
  - Ensures a seamless process for ticket acquisition.

## Buy Tickets Page  [Lee Wei Ying]

- **Image View of Map Including Seat Number**:
  - Displays a visual map of the venue with seating arrangements.
  - Allows users to see the layout and select their preferred seats.

- **Dropdown for Different Categories and Seat Numbers**:
  - Populated from data stored in Firebase.
  - Users can select a seat category first, which filters available seat numbers.

- **Seat Selection Logic**:
  - Users can only choose a seat number after selecting a seat category.
  - Once both seat category and number are chosen, the bottom information will autofill.

- **Edit View for Ticket Quantity**:
  - Allows users to enter the number of tickets they wish to purchase.

- **Toast Messages**:
  - Alerts users of their choices.
  - Ensures users enter a quantity greater than 0.
  - If the user changes the seat category, the previously selected seat number will disappear, and the book button will be hidden to enforce the selection of a new seat number.

- **Book Button**:
  - Appears only when all information in the table is filled.
  - Ensures all necessary selections are made before proceeding.

## Payment Details Page [Koh Ye Chyang]

- **Card and Billing Information**:
  - Allows users to enter necessary card details and billing address.
  - Dropdown to allow users to choose their preferred card type.

- **View Booking Button**:
  - Opens a dialogue that shows booking information made by the user from the previous page.

- **Total Price TextView**:
  - Displays the total price the user has to pay.

- **Buy Now Button**:
  - Allows the user to complete the purchase.

- **Toast Messages**:
  - Alerts users if they enter any field incorrectly.

- **Cancel Button**:
  - Leads users back to the buy tickets page.
    
- **Feedback Popup**:
   - Prompts users for feedback
   - Has Yes or No option
     
## Feedback Page [Koh Ye Chyang]

- **Feedback Form**:
  - Allows users to enter their feedback in a message field.
  - Provides a dropdown from the bottom to choose the type of feedback, such as experiences, bug reports, feature requests, or performance issues.

- **Attach Image Button**:
  - Allows users to attach an image from their file system to support their feedback.
 
- **RecyclerView of Attached image**
  - Displays a list of image that the user attached

- **Submit Feedback Button**:
  - Sends the user back to the home page after submitting feedback.
  - Ensures feedback is successfully recorded and user is redirected.

- **Access Restrictions**:
  - Can only be accessed upon successful payment and by pressing "Yes" on the popup that prompts for feedback.

## Footer Navigation [NG Joe Yi]
- The footer navigation includes icons that links to the **Homepage**, **Explore Events**, **Booking History**, and **Profile Page** for easy access and seamless user experience.

## Explore Event Page [Tan Si Huei Chloe, Ng Joe Yi]
- **Event Search [Tan Si Huei Chloe]**:
  - Allows users to search events by title or artist.
  - Provides a user-friendly interface for quick event discovery.

- **Filter Options [Ng Joe Yi]**:
  - Users can filter events by price, event type, or date.
  - Price and event type options are dynamic and depend on the database.
  - Users can clear the selected filters by using the "Clear Filters" button.
  - Ensures users can find events that meet their preferences.
  - Made scrollable to ensure responsiveness in landscape orientation.

- **RecyclerView of Events [Tan Si Huei Chloe]**:
  - Displays a list of different events at the bottom of the page.
  - Events are dynamically populated from Firebase.

## Profile Page [Koh Yung Chun Ivan]
- **View Account Information**:
  - Displays the user's account information.
  - Data is dynamically populated from Firebase, ensuring it is always up-to-date.

- **Edit Profile**:
  - Users can edit their profile information by pressing the pencil icon next to the fields.
  - Provides an intuitive and user-friendly way to make changes to personal details.

- **Password Visibility Toggle**:
  - Users can check the checkbox to show their password.
  - Allows users to verify their input for accuracy, enhancing security and reducing login erros.

- **Logout Button**:
  - Allows users to log out of their account.
  - Provides a secure way to end the session.

- **Save Button**:
  - Allows users to save updated account information.
  - Ensures changes are stored and reflected in their profile and stored in Firebase.

- **View Feedback Page**:
  - Accessed only through the profile page, ensuring it is secure.
  - Displays feedback in cards format for easy viewing.
  - Includes an exit button that returns users to the profile page.

## Booking History Page [Koh Ye Chyang]
- **View Booking History**:
  - Shows the booking history of the user, populated from Firebase.
  - Provides a comprehensive view of all bookings made by the user.

- **Booking Information Cards**:
  - Each card displays detailed information about a booking, including:
    - **Seat Category**
    - **Seat Number**
    - **Total Price**
    - **Quantity**
    - **Payment Method**
  - Ensures users have easy access to their booking details.

- **RecyclerView of Booking details**
  - Displays a list of booking details that user had booked
  - Booking details are dynamically GET from the firebase for the specific user

### Responsiveness implementation [Lee Wei Ying, Ng Joe Yi , Tan Si Huei Chloe]

### Logo Implementation [Lee Wei Ying]


# Ticket Finder App Stage 2 Features

## Google Maps of the venue [CHLOE]
- **Google Map Fragment of the venue**:
  - Displays a map fragment with a pin on the event venue
  - Automatically zoomed in on the event venue area to display all necessary labels and surrounding details
  - Allows user the option to display different map type views (Normal, Satellite, Terrain, Hybrid)
 
- **Google Maps Navigation**:
  - Allows user to find the best route to the event venue from their current location
  - Clicking "Go to Google Maps" will open the Maps application in navigation mode, with the event venue automatically input as the destination
    
- **Dynamic Venue Details Retrieval**:
  - Retrieves the full venue name, latitude and longtitude using Geocoder class
  - Approximates the venue by reading the input venue name (eg. 'Victoria Concert Hall') and choosing the closest match
  - Similarly retrieves the Place ID of the venue using Geocoding API
  - Retrieves the venue details from Places API using the retrieved Place ID
 
- **Event Venue Details - Google Places API**:
  - Dynamically displays the following details if they are available:
    - RecyclerView of all available venue images
    - Opening Hours
    - Contact/Phone number
    - Website Link
    - Wheelchair accessibility
    - Average rating and total rating count
    - RecyclerView of reviews left by users. Included details:
      - Review author
      - Review time (relative to current time)
      - Given Rating
      - Review Text
 
- **Nearby Places - Google Places API**
  - Allows users to find nearby places of interest (restaurants, parking, public transit)
  - Type of nearby places is selectable using radio buttons, with the option of hiding all nearby places markers
  - All relevant nearby places are marked with blue coloured markers on the Google Map fragment, distinguishing them from the event marker  


## Biometric Integration [Koh Yung Chun Ivan]
Biometric authentication has been implemented in multiple sections of the Ticket Finder app to enhance security and user convenience. The biometric features include:

### Sign In Page
- **Biometric Login**:
  - Users can authenticate their identity using biometric credentials (fingerprint or face recognition) to log in quickly and securely.
  - Biometric prompts are integrated to retrieve saved email and password for autofill.
  - Implementation: `SignIn.java` utilizes `BiometricPrompt` to handle biometric authentication and credential retrieval, ensuring a seamless and secure login process.

### Profile Page
- **Biometric Authentication for Editing Profile**:
  - Users can enable biometric authentication to edit their profile information, ensuring only authorized changes are made.
  - Before allowing profile edits, the app prompts the user for biometric authentication.
  - Implementation: `profilePage.java` includes biometric prompts to enable editing and updating user information, such as changing the password and uploading profile pictures securely.

### Payment Page
- **Biometric Autofill for Payment Details**:
  - During the payment process, biometric authentication is used to autofill saved card details, making the process faster and more secure.
  - Users are prompted to use biometric authentication to retrieve saved payment information.
  - Implementation: `payment.java` uses `BiometricPrompt` for retrieving and autofilling saved payment details, enhancing the security and convenience of the payment process.

## Image Upload for Profile Pictures [Koh Yung Chun Ivan]
Users can now upload or capture their profile pictures directly within the app. This feature allows users to personalize their profiles with ease.

### Profile Picture Upload
- **Upload or Capture Photo**:
  - Users can choose between taking a new photo or selecting one from the gallery.
  - The selected image is uploaded to Firebase Storage and updated in Firestore.
  - Implementation: `profilePage.java` handles image upload functionality, including permission checks for camera and storage access, and updates the profile picture URL in Firestore.

## Enhanced Stage 1 Feature: User Event Preferences [Koh Yung Chun Ivan]
The user event preferences feature has been enhanced to better tailor event recommendations based on user interests.

### Setting and Saving Preferences
- **Prompt for Preferences**:
  - When users log in for the first time or if preferences are not set, they are prompted to select their favorite genres from a list.
  - The selected preferences are saved in Firestore.
  - Implementation: `MainActivity.java` checks user preferences on login and prompts for preferences if not already set, ensuring relevant event recommendations are provided to the user.

### Utilizing Preferences for Recommendations
- **Personalized Event Recommendations**:
  - Event recommendations on the home page are tailored based on the saved user preferences.
  - Events matching user preferences are prioritized and displayed prominently.
  - Implementation: The home page fetches user preferences from Firestore and uses them to filter and display recommended events.

## Speech Navigation [Koh Yung Chun Ivan]
Speech recognition capabilities have been integrated into the app to enhance accessibility and user interaction.

### Voice Command Navigation
- **Speech Recognition**:
  - Users can navigate the app and perform actions using voice commands.
  - Converts spoken language into text for processing, making it easier for users to interact with the app without relying on manual input.
  - Implementation: `SpeechRecognitionNav.java` implements speech recognition using `SpeechRecognizer` and processes voice commands to navigate the app.

### Navigation Based on Voice Commands
- **Interpreting Commands**:
  - The app interprets voice commands to navigate to different pages such as home, explore events, booking history, or profile page.
  - Can also recognize specific event titles to navigate directly to event details.
  - Implementation: The `getNavigationCommand` method in `SpeechRecognitionNav.java` processes the recognized speech text and determines the appropriate navigation action.

### Integration with Home Page
- **Voice Command Activation**:
  - The home page includes a button to activate speech recognition.
  - Users can use voice commands to navigate the app, enhancing accessibility and ease of use.
  - Implementation: `homepage.java` sets up speech recognition and handles navigation based on recognized voice commands.

    
## Chatbot with Translation and speech to text capabilities [JOEYI]
### Multilingual Support - Google Cloud Translation API
- Automatically detects the language of the user input and responds accordingly.
- Provides translations for both questions and answers, ensuring seamless communication in various languages.
- Ensures accurate and relevant responses by processing the messages in the correct language.

### Speech-to-Text Integration
- Allows users to interact with the chatbot using voice commands.
- Converts spoken language into text for processing, enhancing accessibility and ease of use.

### Event Information Retrieval
- Provides detailed information about various events, including start times, locations, and artist details.
- Can answer specific questions related to events such as ticket purchase, refund policies, and accessibility options.

### Smart Reply Suggestions - Firebase ML Smart Reply API
- Utilizes Firebase Smart Reply to suggest relevant responses based on the conversation context.

### Keyword Matching
- Uses Levenshtein Distance algorithm to find the closest matching keyword from the user input.
- Ensures accurate responses even with minor typos or variations in the user's questions.

### Suggested Prompts
- Displays a list of suggested prompts to guide users on what they can ask.
- Prompts are translated into the detected language for better user understanding.

### FAQ Integration
- Fetches frequently asked questions (FAQs) from the Firestore database.
- Provides quick and accurate answers to common queries related to events and services.

### User-Friendly Interface
- Features an intuitive and easy-to-navigate user interface.
- Provides a smooth and engaging user experience with clear and concise responses.

## Add Event to Calendar [Lee WEI YING]

### Google Calendar Integration
- Allows users to add their event to Google Calendar after payment.
- Requires users to log in to their Google account.
- Redirects users to the Google Calendar app, where concert details such as location, event title, and time are automatically filled in.

## Upcoming Concert Page [Lee WEI YING]

### User-Friendly Interface
- Provides an intuitive interface for users to filter and view their upcoming concerts.

### Nested Adapters for Tickets
- Utilizes nested adapters to manage and display tickets effectively.

### Transfer Tickets Button
- Features a button enabling users to transfer tickets to friends.

### Dynamic Concert Management
- **Remove Concerts**:
  - Automatically removes concerts from the list if no tickets are available.
- **Add New Concerts**:
  - Adds new concerts to the adapter if they become available.
- **Update Existing Concerts**:
  - Updates the details of concerts in the adapter if they were previously listed.

## User-Friendly Payment Page [Lee WEI YING]

### Quantity Entry Requirement
- Requires users to enter the desired quantity of tickets before selecting seat category and seat number.

### Dynamic Seat Selection
- Displays the corresponding number of seat selection adapters after entering the desired quantity, allowing users to specify seat category and seat number.

### Conditional Book Button
- The "Book" button appears only after all required fields (quantity, seat category, and seat number) have been filled in, ensuring users complete all necessary selections.

## Transfer Tickets [Lee WEI YING]

### Ticket Transfer
- Enables users to transfer tickets to friends from a list of their contacts.
- Users can select specific tickets to transfer.

## Add Friend [Lee WEI YING]

### Add Friend Interface
- Provides an easy-to-use interface for adding friends.
- Includes a search container to find friends by name.

## Unfriend Friend [Lee WEI YING]

### Unfriend Interface
- Allows users to unfriend individuals through a user-friendly interface.
- Features a search container to find friends by name.
    
## Implement weather forecast API for dates of events [KOH YE CHYANG]
### 4 Day Weather Forecast & 2-Hour Weather Forecast
- Integrated APIs to fetch weather data for 4 day weather forecast and 2 Hour weather forecast, providing weather information for the next 2 Hours or 4 days.
- Ensures accurate and detailed data like area, wind, wind direction, humidity, latitude and longtitude.

### Pagination RecyclerView Integration at 2-Hour Weather Forecast
- More efficient data management to handle large amount of data more efficiently instead of normal recyclerview.
- Improved performance (Load faster) and responsiveness of the app.
- Ensuring smooth and accessible experience for users.

### Weather Implementation at bookingdetailsmore page
- Automatic date detection and displaying the weather information for that specific event date

## QR Codes [KOH YE CHYANG]
### Generating Qr Codes
- Displaying a QR Code that contains all the booking information inside the QR Code.

### QR Scanner
- Scanning the QR Code generated and displaying all the available information on the QR Code with approval status.
  
### Live Updates and verification
- Ensuring QR Code validity using database firestore.
- Ensuring QR Code belongs to the user that owns it.

## Local Notification [KOH YE CHYANG]
Local Notification has been implemented in different pages of the ticket finder app, the local notification will be sent to their device.

### BookingDetailsMore Page
- **Weather Notification and Reminders**
  - Users can select the date and time they want for the notification to be sent before the Event Date.
  - Users can add any reminders they want.
  - Sent notification will contain all the reminders and the weather details of that event day.

### Weather Page
- **Specifc Area Weather Notification**
  - Users can use the add button beside the weather data to add the specific area and the forecast to the notification
  - Users can select the date and time they want for the notification to be sent to their device.

## ItemTouchHelper/Swipe To Reveal Options [KOH YE CHYANG]
ItemTouchHelper has been implemented to different pages of the ticket finder app

### Forum Page
- Implemented Swipe right to reveal Delete option for user to remove forum post they made.
- Implemented Swipe right to reveal Edit option for user to edit forum post they have made.

### Weather Page
- Implemented Swipe left to delete the reminders.
- Added Snackbar to the ItemTouchHelper to undo the delete they have made.
- Implemented Drag and drop to move the reminders around to be displayed for the notification.

### Bottom Sheet Implementation to Forum Page
- Displaying comment section for each forum post.
- Providing user with live updates for the post and the comment ensuring consistency and accuracy.
- Ensuring smooth and accessible experience for users.

## Credit
- Event images generated by ChatGPT
  - prompt: "give me a concert poster of ${artist}"
- Seating map: https://www.google.com/imgres?imgurl=https%3A%2F%2Fwww.sportshub.com.sg%2Fsites%2Fdefault%2Ffiles%2F2024-05%2FSIS-OliviaRodrigo-seatmap-V1_0.jpg&tbnid=ya5L3yXIy85tdM&vet=1&imgrefurl=https%3A%2F%2Fwww.sportshub.com.sg%2Fevents%2Folivia-rodrigo-guts-world-tour-singapore&docid=IN8FmSGAZ6FiZM&w=1081&h=1115&hl=en-SG&source=sh%2Fx%2Fim%2Fm6%2F4&kgs=344d0338f3be90bf&shem=abme%2Cssic%2Ctrie
- Footer icon: https://www.canva.com/
- Ai robot icon: https://lottiefiles.com/
- 4 Day Weather Forecast: https://beta.data.gov.sg/collections/1456/datasets/d_1efe4728b2dad26fd7729c5e4eff7802/view
- 2-hour Weather Forecast: https://beta.data.gov.sg/collections/1456/datasets/d_91ffc58263cff535910c16a4166ccbc3/view
- Scanner Icon: https://www.flaticon.com/free-icon/barcode-scanner_4379661?term=qr+scanner&page=1&position=3&origin=search&related_id=4379661
