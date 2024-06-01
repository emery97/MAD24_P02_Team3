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
3. S10262850 Koh Yung Chun Ivan
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
  - Made scrollable to ensure responsiveness in horizontal orientation.

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

## Features Allocation
### Stage 2 
1. Google Maps of the venue (CHLOE)
2. When changing profile picture, allow user to take picture using the camera (IVAN)
3. Voice recording and recognition (Speech-to-Text) for search (JOEYI)
4. Add event to calendar app  (WEIYING)
5. Implement weather forecast API for dates of events (YE CHYANG)

## Credit
- Event images generated by ChatGPT
- seating map: https://www.google.com/imgres?imgurl=https%3A%2F%2Fwww.sportshub.com.sg%2Fsites%2Fdefault%2Ffiles%2F2024-05%2FSIS-OliviaRodrigo-seatmap-V1_0.jpg&tbnid=ya5L3yXIy85tdM&vet=1&imgrefurl=https%3A%2F%2Fwww.sportshub.com.sg%2Fevents%2Folivia-rodrigo-guts-world-tour-singapore&docid=IN8FmSGAZ6FiZM&w=1081&h=1115&hl=en-SG&source=sh%2Fx%2Fim%2Fm6%2F4&kgs=344d0338f3be90bf&shem=abme%2Cssic%2Ctrie
- footer icon: https://www.canva.com/