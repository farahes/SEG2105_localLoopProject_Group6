# [SEG2105-Z (2025/5)] LocalLoop Project (Group 6)


## OUR MORAL CODEX:
> Spaghetti logic today is tomorrow’s blocked deliverable.


## Collaborators:
  - Farah El Siss     - felsi072@uottawa.ca (#300168267)
  - Derik Thiessen    - dthie071@uottawa.ca (#300231666)
  - Giovanni Abbruzzo - gabbr085@uottawa.ca (#300233180)
  - Mariia Yermolenko   - myerm102@uottawa.ca (#300404297)
  - Dany Nuriev       - dnuri029@uottawa.ca (#300429224)


## Link to GitHub:
https://github.com/farahes/SEG2105_localLoopProject_Group6/tree/main


## 🔐 Test Accounts

| Role        | Email / Username | Password  | Comments                                      |
|-------------|------------------|-----------|-----------------------------------------------|
| Admin       | admin            | XPI76SZUqyCjVxgnUjm0 | Admin account                       |
| Organizer   | organizer        | 11111111  | Main organizer account with events added       |
| Organizer   | organizerTwo     | 11111111  | No events (please **do not** add any events)   |
| Participant | participant      | 11111111  | Regular participant account                    |


## 🔍 Brief Overview:

LocalLoop is a role-based community event management app built in Java for Android. It allows Admins, Organizers, and Participants to interact through a unified, scalable architecture.

### 👥 User Roles
1. **Admin**: Manages user accounts by enabling, disabling, or deleting them.
2. **Organizer**: Creates, edits, and deletes local events under predefined categories.
3. **Participant**: Browses events, requests to join, and receives approval or rejection.

### 📱 Core Features
1. Role-specific dashboard views and logic.
2. Firebase-based user and event persistence.
3. Clean architecture using Activities, Services, Repositories, and DTO models.

### 🛠 Technologies
1. Android Studio Koala (API 33+)
2. Java & Kotlin
3. Firebase Realtime Database

### Color Scheme Used
Blue: #62CDD9\
Purple Primary: #673AB7\
Secondary Lighter Purple: #8F50E7\
Tertiary Grey: #8F50E7\
Tertiary Black: #373D3F (Used for text)

### Figma Wirefranes
- Used to plan our design.
- Link: TBD

`Version: 07-04-2025`

## 📱 App Flow Screenshots 

#### Login and Create Account Pages:

<img width="250" height="1402" alt="image27" src="https://github.com/user-attachments/assets/2a9d2b7f-acb0-4ec4-b808-66df5430d0c1" />
<img width="250" height="1268" alt="image30" src="https://github.com/user-attachments/assets/2405caaf-6850-44ac-8db8-3073250c3514" />
<img width="250" height="1272" alt="image19" src="https://github.com/user-attachments/assets/d25dabc6-5582-4b8d-b8eb-7fe6c6d064b2" />


---

### Organizer Account User Flow

#### Organizer Dashboard:
*Note: Red exclamation mark icon next to “manage registrations” only comes up when there is a new registration.*

<img width="250" height="1999" alt="image11" src="https://github.com/user-attachments/assets/efc6d3c8-06d8-4d06-90b6-27b8a499b578" />

#### Clicking on a Date with a Dot in the Calendar – Pop-up:

<img width="250" height="1404" alt="image7" src="https://github.com/user-attachments/assets/7476e4ca-53b5-4f69-b95e-aa5f8dad2ed4" />

#### No Events and Registrations Placeholder:

<img width="250" height="1999" alt="image18" src="https://github.com/user-attachments/assets/e5df954d-c1a5-4374-b3ca-cd1c7b28be98" />
<img width="250" height="1999" alt="image35" src="https://github.com/user-attachments/assets/240e9c45-d0ef-47f4-bb45-ad364a0667fd" />

#### Creating Events After Clicking “Create New Event” in the Dashboard:

<img width="250" height="1999" alt="image15" src="https://github.com/user-attachments/assets/8a9474d1-44b6-4d45-be97-48b25bec7aca" />
<img width="250" height="1268" alt="image24" src="https://github.com/user-attachments/assets/a843510b-753f-431f-bc6d-5eba9830eddd" />

#### Managing Organizer Events:

<img width="250" height="1999" alt="image29" src="https://github.com/user-attachments/assets/8474e852-057f-4009-bd6f-bdb94c11b841" />
<img width="250" height="1999" alt="image6" src="https://github.com/user-attachments/assets/2d7c8904-33d5-472c-99a8-b7e3c88d0d04" />

#### Managing Event Registrations:
*Note: the accepted participants list shows the username only, while the request card shows the set name and username. The “#/0” shows the number of participants accepted to an event. 0 indicates there is no maximum to the event set yet.*

<img width="250" height="1999" alt="image5" src="https://github.com/user-attachments/assets/6b150524-587e-4e4f-bb7c-7ec83ad7543d" />

#### Account Page – View Account Information and Log Out:

<img width="250" height="1999" alt="image16" src="https://github.com/user-attachments/assets/4305844d-e886-4cea-b768-1011a4b95326" />

---

### Participant Account User Flow

#### Participant Dashboard:

<img width="250" height="1999" alt="image21" src="https://github.com/user-attachments/assets/2be89309-e281-4efb-9566-a6bae09d19ca" />

#### Searching for Events as a Participant (After Pressing “Search Events” and Selecting Different Filters):

<img width="250" height="1999" alt="image2" src="https://github.com/user-attachments/assets/3c7f6813-84f6-4aaa-bd78-42aa2287f3dd" />
<img width="250" height="1406" alt="image10" src="https://github.com/user-attachments/assets/c85aaad6-4992-4c1e-990c-dd965feebcd9" />
<img width="250" height="1114" alt="image31" src="https://github.com/user-attachments/assets/e342c2fd-c46f-4de1-8649-4a0be39c6b87" />
<img width="250" height="671" alt="image32" src="https://github.com/user-attachments/assets/af985dce-e737-46fa-86ab-d0f95fa17459" />
<img width="250" height="1057" alt="image22" src="https://github.com/user-attachments/assets/6b561c46-fb54-4a46-ad92-4ffcd6bb3be9" />
<img width="250" height="780" alt="image8" src="https://github.com/user-attachments/assets/934b172b-a835-4945-8217-cb0471a9a771" />

<img width="250" height="1403" alt="image4" src="https://github.com/user-attachments/assets/90986bd1-8110-4dce-ba50-500931755ad7" />
<img width="250" height="1401" alt="image17" src="https://github.com/user-attachments/assets/cc50e977-8f66-4e8c-a3f5-11123e2ba401" />
<img width="250" height="1999" alt="image39" src="https://github.com/user-attachments/assets/1be6bf52-b270-4bf5-8d7c-26f4e3aecbdf" />

#### Registering for Events – Showing My Tickets (from Navigation Bar):
*After pressing register, a Pop-Up Occurs.*

<img width="250" height="1408" alt="image1" src="https://github.com/user-attachments/assets/35cc890c-b33e-4c8a-a36d-6387d19b81b8" />
<img width="250" height="946" alt="image20" src="https://github.com/user-attachments/assets/a0c3cb5b-dd67-4a4e-a441-00a89e7a017d" />

#### My Tickets Tab:
*Note: this is just UI and mock events. Future implementations will show upcoming and past registrations of events that the participant has or had.*

<img width="250" height="1393" alt="image33" src="https://github.com/user-attachments/assets/90cc688e-f6bf-47fb-a85f-b59b295888f2" />

#### Account Page – View Account Information and Log Out:

<img width="250" height="1999" alt="image14" src="https://github.com/user-attachments/assets/6d217428-a34a-48e4-a1d3-359abc5a4b92" />

---

### Admin Account User Flow

#### Admin Dashboard and Managing User Accounts:
*Note: Home button and inbox button are placeholders for now.*

<img width="250" height="1377" alt="image13" src="https://github.com/user-attachments/assets/bc47b104-0dbf-4d4e-b351-18d4d9e4a34b" />
<img width="250" height="899" alt="image37" src="https://github.com/user-attachments/assets/6a4f9554-775e-4450-8a3b-57aaedb96491" />


#### Manage Categories:

<img width="250" height="1404" alt="image12" src="https://github.com/user-attachments/assets/529d7532-8e01-4a5b-8157-6d8068b71489" />
<img width="250" height="1409" alt="image28" src="https://github.com/user-attachments/assets/0f9a16f7-c836-4e6c-a3a8-f0362ae8cee6" />
<img width="250" height="1411" alt="image9" src="https://github.com/user-attachments/assets/968f5ac2-80e8-4b98-b960-1e239cc013c0" />

#### Account Page – View Account Information and Log Out:

<img width="250" height="1999" alt="image36" src="https://github.com/user-attachments/assets/454b5e54-e81e-4477-b9f0-85ab152dc621" />

---

### Summary of Major Additional Features Implemented

The following features are not necessarily a requirement set by the project, but were implemented to improve the user experience of the app:

- Account and Logout Pages
- Home Button
- Organizer Dashboard Calendar (Clicking each date shows the events occurring on that date)
- Uploading and downloading images to Firebase through unconventional means
- Event Card Layout / Pop-up
- Google Maps API (Showing location on Map view for each event on the event card)




