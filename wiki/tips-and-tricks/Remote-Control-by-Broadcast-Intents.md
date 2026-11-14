**Important Notice**
Remote control by broadcast intents has to be enabled under "Settings" > "Behaviour" before Universal Sync listens to broadcast intents sent by third-party automation apps.

Universal Sync can be controlled externally by sending Broadcast-Intents. Applications like **Tasker**, **Llama** or **Automate** now can _start_ or _stop_ Universal Sync on behalf of the user.
Use cases would be to run Universal Sync only in special conditions - like at home and charging, or once every night, ...

${applicationId} = com.universalsync.android

The following intent actions are available:
* Let Universal Sync Follow Run Conditions
`adb shell am broadcast -a ${applicationId}.action.FOLLOW -p ${applicationId}`

* Force Start Universal Sync
`adb shell am broadcast -a ${applicationId}.action.START -p ${applicationId}`

* Force Stop Universal Sync
`adb shell am broadcast -a ${applicationId}.action.STOP -p ${applicationId}`

The intents should be set to 'broadcast' rather than starting an activity of service. Note that some apps, e.g. **Llama**, are sensitive to trailing spaces so be careful not to leave any when entering the action.

Tasker example action to start Universal Sync:
* Action: Send Intent
```
Action: ${applicationId}.action.START
Type: None
Mime type: [ leave empty ]
Data: [ leave empty ]
Extra: [ leave empty ]
Package: ${applicationId}
Class: [ leave empty ]
Target: Broadcast Receiver
Description: Start Universal Sync
```

For the **Automate** app there is an example-flow available in the Automate-Community that demonstrates the start- and the stop-intent. Search for *Universal Sync*.
