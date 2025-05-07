function sendSOS() {
  if (navigator.geolocation) {
    document.getElementById('sosStatus') && (document.getElementById('sosStatus').textContent = 'Sending SOS...');
    
    navigator.geolocation.getCurrentPosition(
      position => {
        const data = {
          latitude: position.coords.latitude.toFixed(6),
          longitude: position.coords.longitude.toFixed(6),
          time: new Date().toLocaleTimeString()
        };
        
        // Create a shareable message
        const message = `📍 SOS Alert!\nLatitude: ${data.latitude}\nLongitude: ${data.longitude}\nTime: ${data.time}`;
        
        // For mobile, provide option to share
        if (navigator.share) {
          navigator.share({
            title: 'SOS Emergency Alert',
            text: message,
            url: `https://www.google.com/maps?q=${data.latitude},${data.longitude}`
          }).then(() => {
            if (document.getElementById('sosStatus')) {
              document.getElementById('sosStatus').textContent = 'SOS shared successfully!';
            } else {
              alert('SOS shared successfully!');
            }
          }).catch(error => {
            alert(`📍 SOS Alert!\nLatitude: ${data.latitude}\nLongitude: ${data.longitude}\nTime: ${data.time}`);
          });
        } else {
          // Fallback for browsers without Web Share API
          alert(`📍 SOS Alert!\nLatitude: ${data.latitude}\nLongitude: ${data.longitude}\nTime: ${data.time}`);
        }
      },
      error => {
        if (document.getElementById('sosStatus')) {
          document.getElementById('sosStatus').textContent = 'Error retrieving location.';
        } else {
          alert("Unable to retrieve your location. Please try again.");
        }
        console.error("Geolocation error:", error);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  } else {
    alert("Geolocation is not supported by your browser.");
  }
}

function startFakeCall() {
  // Show loading status
  const callScreen = document.getElementById("callScreen");
  if (callScreen) {
    callScreen.querySelector('p').textContent = "Calling...";
    callScreen.style.display = "block";
    
    // Simulate connecting delay
    setTimeout(() => {
      callScreen.querySelector('p').textContent = "Mom";
      // Vibrate for mobile devices if supported
      if (navigator.vibrate) {
        navigator.vibrate([200, 100, 200]);
      }
    }, 3000);
  }
}

function endFakeCall() {
  const callScreen = document.getElementById("callScreen");
  if (callScreen) {
    callScreen.style.display = "none";
  }
}

// Detect if device is mobile
function isMobileDevice() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

// Add event listeners when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
  // Add touch feedback for buttons on mobile
  if (isMobileDevice()) {
    const buttons = document.querySelectorAll('button');
    buttons.forEach(button => {
      button.addEventListener('touchstart', function() {
        this.style.transform = 'scale(0.98)';
      });
      
      button.addEventListener('touchend', function() {
        this.style.transform = 'scale(1)';
      });
    });
  }
});
