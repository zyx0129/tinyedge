import yaml

config = {
    "keras-app": {
        "build": "./keras-app",
        "container_name": "edge_keras-app",
        "restart": "always",
        "ports": ["80:5211"],
        "networks": ["edge_network"],
        "volumes": [
            "./keras-app/model:/root/.keras/models/",
        ],
    }
}
if __name__ == '__main__':
    with open("docker-compose.yaml", "w")as file:
        yaml.dump(config, file)
    exit(0)
