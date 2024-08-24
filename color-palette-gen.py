import numpy as np
from sklearn.cluster import KMeans
from PIL import Image
import json
import sys

def extract_colors(image_path, n_colors):
    # Load image
    image = Image.open(image_path)
    image = image.resize((100, 100))
    pixels = np.array(image).reshape(-1, 3)

    # Apply KMeans clustering
    kmeans = KMeans(n_clusters=n_colors).fit(pixels)
    colors = kmeans.cluster_centers_

    return colors.astype(int).tolist()

def main(image_path, n_colors, output_path):
    colors = extract_colors(image_path, n_colors)
    with open(output_path, 'w') as f:
        json.dump(colors, f)

if __name__ == "__main__":
    image_path = sys.argv[1]
    n_colors = int(sys.argv[2])
    output_path = sys.argv[3]
    main(image_path, n_colors, output_path)
