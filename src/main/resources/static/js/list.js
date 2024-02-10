

const createChannel = () => {

    const form = document.getElementById('channel-request-form');
    const inputs = form.querySelectorAll('input');

    const channelRequest = {};

    inputs.forEach(input => {
        channelRequest[input.name] = input.value;
    });

    const url = "/channel"

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(channelRequest)
    })
        .then(response => {
            if(response.ok) {
                return response.json();
            } else {
                console.error("Failed to create channel. Status code: " + response.status);
            }
        })
        .then(responseBody => {
            const { data, resultYn } = responseBody;
            if(!resultYn) {
                throw new Error("채널 생성이 실패하였습니다.");
            } else if(resultYn === "Y") {
                window.location.href = "/channel/" + data;
            }
        })
        .catch(error => {
            console.error("Failed to send request:", error);
        });
}

document.addEventListener("DOMContentLoaded", () => {

    document.querySelector("#submitData").addEventListener("click", createChannel);

})
