
const enterChannel = (channelId) => {
    // TODO HTTP GET /channel/channelId
}

const createChannel = ({name, capacity}) => {

    const url = "/channel"

    const channelRequest = {
        name,
        capacity
    }

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(channelRequest)
    })
        .then(response => {
            if (response.ok) {

                const responseBody = JSON.parse(response.body);

                console.log("Channel created successfully!");
                window.location.href = "/channel/" + responseBody.id;
            } else {
                // 응답이 오류인 경우
                console.error("Failed to create channel. Status code: " + response.status);
            }
        })
        .catch(error => {
            // 네트워크 오류 등으로 요청이 실패한 경우
            console.error("Failed to send request:", error);
        });


}


$(function () {

    $("#enter").click((roomId) => enterRoom(roomId));
});