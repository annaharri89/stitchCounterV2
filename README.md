# Stitch Counter V2
This is an app I previously wrote in Java and have now rewritten in Kotlin and Compose.

# Current Features
- With StitchTr@acker, you are able to create different projects to keep track of stitches and rows/rounds while knitting or crocheting. These projects are saved automatically when you leave the StitchTr@acker page and you can access them again later from the Library page.
- Requires no internet connection, unless you want to share to social media - TODO make sure this is true
- Dark mode depending on your phone's settings

# Future Features
- be able to save photos to your project to see progress
- share your project to social media (can I do this without a server? considering I want to add photos)
- You can choose your color theme based on a number of predefined themes.
- lots of accessibility features
- allow users to sort by more than project title, more than ascending, also descending
- make it so you can delete a project or multiple projects after confirming you want to delete them
- make it so the user can select if it's "Rows" or "Rounds for each project"
- allow users to add a description to their project/ instructions
- you can import/export projects or your whole library (can back things)

# IN GITHUB ISSUES
- rewrite single counter into compose screen
- rewrite double counter into compose screen
- rewrite settings into compose screen
- make a new screen that lets you enter a description/instructions for your project and also let you keep pictures of your project (let them know they can have unlimited pictures, but that it will take up more space on your own phone and be harder to restore)
- be able to save photos to your project
- make it so you can import/export projects or the whole library so they can back up their projects.

# TODOS
- seems crappy that you have to leave the page to get the tracker to save. look into how to do this better. Maybe I could save changes to shared preferences and then save them to the db when they get back to the app and delete them from shared preferences when you save to the db successfully.
- update the license with all the stuff you used (google fonts, picaso image loading, etc)
- make sure you don't need an internet connection
- change app icon to be better
- make an about in settings and include the license and also talk about how this app used to be Stitch Counter and show the old icon
- provide an FAQ in the settings
- add progress bar, description, and photo preview to library preview
- handle horizontal vs portrait in compose
- color theme using compose themes, you can choose your theme
- share your project to social media (can I do this without a server? considering I want to add photos)
- color theme using compose themes, you can choose your theme
- lots of accessibility features
- allow users to sort by more than project title, more than ascending, also descending
- make it so you can delete a project or multiple projects after confirming you want to delete them
- make it so the user can select if it's "Rows" or "Rounds for each project"
- bug: the counter projects lose progress if you go directly to the library page
- once you've solidified the color scheme, make sure three button navigation phones looks right
- make sure you're handling the db correctly, does it need closed at any point?
- maybe I should have a set up intro for new users where they get to choose whether to use the default color scheme or to choose their own color scheme?
- do I need shared preferences at all in different view models or anything like that? If so I should implement dagger injection and provide the shared preferences stuff
- make sure each file has the copy right Anna Harrison stuff
- make the theme changing screen have a boxes with the colors like on the website you found the colors on and when you tap them they change the theme